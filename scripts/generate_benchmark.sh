#!/bin/bash

# Description:
# This script generates benchmark-specific JSON documents using cluster data
# retrieved from the get_cluster.sh script. Each output includes the benchmark
# type and embedded cluster metadata. It supports chunk size overrides.

# Usage information
usage() {
	echo "Usage: $0 --saunafs_cluster_ip=IPv4 --benchmark=\"latency,bandwidth\" [--chunks_per_disk=N] [--size=BYTES]"
	echo ""
	echo "Options:"
	echo "  --saunafs_cluster_ip=IPv4  IP address of the SaunaFS admin node"
	echo "  --benchmark=\"...\"        List of benchmarks, comma or space separated"
	echo "  --chunks_per_disk=N        Limit number of chunks to read per disk"
	echo "  --size=BYTES               Override chunk sizes in cluster JSON (only if all chunks >= BYTES)"
	echo "  -h, --help                 Show this help message and exit"
	echo "  -v, --verbose              Enable verbose output"
	exit 1
}

# Parse arguments
SAUNAFS_CLUSTER_IP=""
BENCHMARKS=()
VERBOSE=0
CHUNKS_PER_DISK=""
TARGET_CHUNK_SIZE=""

# Check help
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
	usage
fi

# Argument parsing
for arg in "$@"; do
	case $arg in
		--saunafs_cluster_ip=*)
			SAUNAFS_CLUSTER_IP="${arg#*=}"
			;;
		--benchmark=*)
			raw_benchmarks="${arg#*=}"
			# Replace commas with spaces and split into array
			IFS=' ' read -r -a BENCHMARKS <<< "${raw_benchmarks//,/ }"
			;;
		--chunks_per_disk=*)
			CHUNKS_PER_DISK="${arg#*=}"
			;;
		--size=*)
			TARGET_CHUNK_SIZE="${arg#*=}"
			;;
		--verbose|-v)
			VERBOSE=1
			;;
		*)
			echo "Unknown argument: $arg" >&2
			usage
			exit 1
			;;
	esac
done

# Validation
if [[ -z "${SAUNAFS_CLUSTER_IP}" || ${#BENCHMARKS[@]} -eq 0 ]]; then
	usage
fi

# Verbose info
if [[ ${VERBOSE} -eq 1 ]]; then
	echo "SaunaFS Cluster IP: ${SAUNAFS_CLUSTER_IP}" >&2
	echo "Benchmarks: ${BENCHMARKS[*]}" >&2
	[[ -n "${CHUNKS_PER_DISK}" ]] && echo "Chunks per disk: ${CHUNKS_PER_DISK}" >&2
	[[ -n "${TARGET_CHUNK_SIZE}" ]] && echo "Requested chunk size: ${TARGET_CHUNK_SIZE}" >&2
	echo "Retrieving cluster chunk data using get_cluster.sh..." >&2
fi

# Build get_cluster.sh args
GET_CLUSTER_ARGS="--quiet"
[[ ${VERBOSE} -eq 1 ]] && GET_CLUSTER_ARGS=""
[[ -n "${CHUNKS_PER_DISK}" ]] && GET_CLUSTER_ARGS="${GET_CLUSTER_ARGS} --chunks_per_disk=${CHUNKS_PER_DISK}"

CLUSTER_JSON=$(./get_cluster.sh "${SAUNAFS_CLUSTER_IP}" ${GET_CLUSTER_ARGS})
if [[ -z "${CLUSTER_JSON}" ]]; then
	echo "Error: Failed to retrieve cluster JSON from get_cluster.sh" >&2
	exit 2
fi

# Apply chunk size override if requested
if [[ -n "${TARGET_CHUNK_SIZE}" ]]; then
	if [[ ${VERBOSE} -eq 1 ]]; then
		echo "Applying size override on chunks..." >&2
	fi

	# Check for chunks too small
	TOO_SMALL=$(jq --argjson size "${TARGET_CHUNK_SIZE}" '
		map(.disks[]?.chunks[]? | select(.size < $size)) | length
	' <<< "${CLUSTER_JSON}")
	if (( TOO_SMALL > 0 )); then
		CHUNK_IDS=$(jq -r --argjson size "${TARGET_CHUNK_SIZE}" '
			.[] | .disks[]?.chunks[]? | select(.size < $size) | .id
		' <<< "${CLUSTER_JSON}")
		echo "Error: The following chunk(s) have size smaller than requested size (${TARGET_CHUNK_SIZE}):" >&2
		echo "${CHUNK_IDS}" >&2
		exit 3
	fi

	# Apply override
	CLUSTER_JSON=$(jq --argjson size "${TARGET_CHUNK_SIZE}" '
		map(
			.disks |= map(
				.chunks |= map(
					.size = $size
				)
			)
		)
	' <<< "${CLUSTER_JSON}")
fi

# Generate benchmark JSONs
for BENCH in "${BENCHMARKS[@]}"; do
	[[ ${VERBOSE} -eq 1 ]] && echo "Generating JSON for benchmark: ${BENCH}" >&2
	jq -n --arg bench "${BENCH}" --argjson cluster "${CLUSTER_JSON}" \
		'{benchmark: $bench, cluster: $cluster}' > "benchmark_${BENCH}.json"
	echo "Generated benchmark_${BENCH}.json"
done

