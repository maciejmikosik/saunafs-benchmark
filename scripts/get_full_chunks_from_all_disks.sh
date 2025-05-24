#!/bin/bash

# Description:
# This script connects to each SaunaFS chunkserver disk, identifies chunk files (*.dat),
# and extracts their ID, version, size, and type. Output is formatted as JSON.

# Configuration: Admin IP and port to get disk listing
SAUNAFS_CLUSTER_IP="$1"
if [[ -z "${SAUNAFS_CLUSTER_IP}" ]]; then
  echo "Usage: $0 <SAUNAFS_CLUSTER_IP> [--chunks_per_disk=N] [--output=FILE]" >&2
  exit 1
fi
ADMIN_PORT="9421"
SAUNAFS_ADMIN="saunafs-admin"

# Parse arguments
CHUNKS_PER_DISK="ALL"
OUTPUT_FILE="-"
shift
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
	${SAUNAFS_ADMIN} list-disks ${SAUNAFS_CLUSTER_IP} ${ADMIN_PORT} \
		| grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+:[0-9]+:/' \
		> "${DISK_LIST}"
}

# Function to scan a single disk
scan_disk() {
	ENTRY="$1"
	SERVER=$(echo "${ENTRY}" | cut -d: -f1)
	PORT=$(echo "${ENTRY}" | cut -d: -f2)
	DISK_PATH=$(echo "${ENTRY}" | cut -d: -f3-)
	LABEL=$(echo "${SERVER}_${PORT}_$(basename "${DISK_PATH}")" | tr '/' '_')
	OUTFILE="${RESULTS_DIR}/${LABEL}"

	ssh "${SERVER}" 'bash -s' -- "${DISK_PATH}" "${CHUNKS_PER_DISK}" 2>/dev/null <<EOF | {
DISK_TO_SCAN="\$1"
LIMIT="\$2"
CURRENT_COUNT=0
FIXED_SIZE_BYTES=67108864 # 64MiB

sudo find "\$DISK_TO_SCAN" -type f -name '*.dat' -size 64M -print0 2>/dev/null | while IFS= read -r -d \$'\0' filepath; do
  if [[ "\$LIMIT" != "ALL" && "\$LIMIT" != "" && "\$CURRENT_COUNT" -ge "\$LIMIT" ]]; then
    break
  fi
  filename=\$(basename "\$filepath")

  if [[ "\$filename" =~ ^.*_([A-F0-9]{16})_([A-F0-9]{8})\.dat\$ ]]; then
    id_hex="0x\${BASH_REMATCH[1]}"
    version_hex="0x\${BASH_REMATCH[2]}"
    type=0
    echo "\${id_hex},\${version_hex},\${FIXED_SIZE_BYTES},\${type}"
    if [[ "\$LIMIT" != "ALL" && "\$LIMIT" != "" ]]; then
      ((CURRENT_COUNT++))
    fi
  fi

done
EOF
		# Local processing of the remote script's output
		FINAL_COUNT=0
		while IFS=',' read -r CHUNK_ID_HEX CHUNK_VERSION_HEX CHUNK_SIZE_BYTES CHUNK_TYPE; do
			if [[ -n "${CHUNK_ID_HEX}" ]]; then
				chunk_id_dec=$((${CHUNK_ID_HEX}))
				chunk_version_dec=$((${CHUNK_VERSION_HEX}))
				echo "${SERVER},${PORT},${DISK_PATH},${chunk_id_dec},${chunk_version_dec},${CHUNK_SIZE_BYTES},${CHUNK_TYPE}"
				((FINAL_COUNT++))
			fi
		done
		echo "${SERVER},${DISK_PATH},${FINAL_COUNT}" >> "${SUMMARY_FILE}"
	} > "${OUTFILE}"
}

# Main
parse_disk_list
> "${SUMMARY_FILE}"

while IFS= read -r ENTRY; do
	scan_disk "${ENTRY}" &
done < "${DISK_LIST}"

wait

JSON_CONTENT=$(cat "${RESULTS_DIR}"/* | sort | jq -Rn '
reduce inputs as $line ([]; if $line == "" then . else . + [$line | split(",")] end)
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
            chunks: map({
                id:      (.[3] | tonumber),
                version: (.[4] | tonumber),
                size:    (.[5] | tonumber),
                type:    (.[6] | tonumber)
            })
        })
    )
})')

if [[ "${OUTPUT_FILE}" == "-" || -z "${OUTPUT_FILE}" ]]; then
  echo "${JSON_CONTENT}"
else
  echo "${JSON_CONTENT}" > "${OUTPUT_FILE}"
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

