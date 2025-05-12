#!/bin/bash

# Description:
# This script connects to each SaunaFS chunkserver disk, runs a find command
# to identify 64MiB chunk files with version 00000001, and extracts chunk IDs.
# Output is formatted as: server,port,disk,chunk_id or as JSON.

# Configuration: Admin IP and port to get disk listing
ADMIN_IP="192.168.50.199"
ADMIN_PORT="9421"
SAUNAFS_ADMIN="saunafs-admin"

# Parse arguments
CHUNKS_PER_DISK="ALL"
OUTPUT_FILE="-"
FORMAT="csv"
for arg in "$@"; do
	case $arg in
		--chunks_per_disk=*)
			CHUNKS_PER_DISK="${arg#*=}"
			shift
			;;
		--output=*)
			OUTPUT_FILE="${arg#*=}"
			shift
			;;
		--format=*)
			FORMAT="${arg#*=}"
			shift
			;;
	esac

done

# Temporary files with unique postfix
TMP_POSTFIX=".$$.$RANDOM"
DISK_LIST="/tmp/saunafs_disk_list.txt.${TMP_POSTFIX}"
RESULTS_DIR="/tmp/saunafs_chunk_results.${TMP_POSTFIX}"
SUMMARY_FILE="/tmp/saunafs_summary.txt.${TMP_POSTFIX}"
mkdir -p "${RESULTS_DIR}"

# Function to parse list-disks output
parse_disk_list() {
	${SAUNAFS_ADMIN} list-disks ${ADMIN_IP} ${ADMIN_PORT} \
		| grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+:[0-9]+:/' \
		> "${DISK_LIST}"
}

convert_64bit_hex_to_int() {
  while IFS= read -r line; do
    while [[ "$line" =~ (0x[0-9a-fA-F]{16}) ]]; do
      hex="${BASH_REMATCH[1]}"
      dec=$((hex))
      line="${line/$hex/$dec}"
    done
    echo "$line"
  done
}

# Function to scan a single disk
scan_disk() {
	ENTRY="$1"
	SERVER=$(echo "${ENTRY}" | cut -d: -f1)
	PORT=$(echo "${ENTRY}" | cut -d: -f2)
	DISK=$(echo "${ENTRY}" | cut -d: -f3-)
	LABEL=$(echo "${SERVER}_${PORT}_$(basename ${DISK})" | tr '/' '_')
	OUTFILE="${RESULTS_DIR}/${LABEL}"

	ssh "${SERVER}" 'bash -s' -- "${DISK}" "${CHUNKS_PER_DISK}" 2>/dev/null <<'EOF' | {
DISK="$1"
LIMIT="$2"

if [[ "$LIMIT" != "ALL" && "$LIMIT" != "" ]]; then
	sudo find "$DISK" -type f -size 64M -name '*_00000001.dat' 2>/dev/null \
	| grep -Eo '_[A-Z0-9]{16}_' \
	| cut -d_ -f2 \
	| sed -r 's/(.*)/0x\1/' \
	| head -n "$LIMIT"
else
	sudo find "$DISK" -type f -size 64M -name '*_00000001.dat' 2>/dev/null \
	| grep -Eo '_[A-Z0-9]{16}_' \
	| cut -d_ -f2 \
	| sed -r 's/(.*)/0x\1/'
fi
EOF
		COUNT=0
		while read -r CHUNK; do
			echo "${SERVER},${PORT},${DISK},${CHUNK}"
			((COUNT++))
		done
		echo "${SERVER},${DISK},${COUNT}" >> "${SUMMARY_FILE}"
	} > "${OUTFILE}"
}

# Main
parse_disk_list
> "${SUMMARY_FILE}"

while IFS= read -r ENTRY; do
	scan_disk "$ENTRY" &
done < "${DISK_LIST}"

wait

if [[ "${FORMAT}" == "JSON" ]]; then
cat "${RESULTS_DIR}"/* | sort | convert_64bit_hex_to_int | jq -Rn '
	reduce inputs as $line ([]; . + [$line | split(",")])
	| group_by(.[0,1])
	| map({
		address: {
			ip: .[0][0],
			port: (.[0][1] | tonumber)
		},
		disks: (
			group_by(.[2])
			| map({
				location: .[0][2],
				name: (.[0][2] | split("/") | last),
				chunks: map(.[3] | tonumber)
			})
		)
	})' > "${OUTPUT_FILE}"
else
	if [[ "${OUTPUT_FILE}" == "-" || -z "${OUTPUT_FILE}" ]]; then
		cat "${RESULTS_DIR}"/* | sort
	else
		cat "${RESULTS_DIR}"/* | sort > "${OUTPUT_FILE}"
	fi
fi

{
	sort "${SUMMARY_FILE}" | awk -F',' -v limit="${CHUNKS_PER_DISK}" '
	{
		server = $1
		count = $3
		chunk_counts[server] = chunk_counts[server] count ","
		chunk_totals[server]++
		if (length(server) > maxlen_server) maxlen_server = length(server)
		if (count > max_chunk) max_chunk = count
		if (min_chunk == "" || count < min_chunk) min_chunk = count
	}
	END {
		max_chunk_len = length(max_chunk)
		col_width = max_disks * (max_chunk_len + 1) - 1
		head1 = "server"
		head2 = "disks"
		head3 = "chunks generated per disks"
		printf "\nSummary:\n"
		printf "%-" maxlen_server "s|%-5s|%-" col_width "s|\n", head1, head2, head3
		for (s in chunk_counts) {
			chunks = chunk_counts[s]
			sub(/,$/, "", chunks)
			split(chunks, arr, ",")
			printf "%-" maxlen_server "s|%5d|", s, chunk_totals[s]
			for (i = 1; i <= length(arr); i++) {
				val = arr[i]
				ratio = 0
				color=""
				if (max_chunk != min_chunk) {
					ratio = (val - min_chunk) / (max_chunk - min_chunk)
					if (ratio < 0.25) color = "\033[1;36m"
					else if (ratio < 0.5) color = "\033[1;32m"
					else if (ratio < 0.75) color = "\033[1;33m"
					else color = "\033[1;31m"
				}
				printf "%s%s\033[0m", color, val
				if (i < length(arr)) {
					printf ","
				}
			}
			print "|"
		}
	} '
} >&2
[[ ${OUTPUT_FILE} != "-" ]] && echo ${OUTPUT_FILE} generated >&2
