#!/bin/bash

# Description:
# This script generates benchmark-specific JSON documents using cluster data
# retrieved from the get_cluster.sh script. Each output includes the benchmark
# type and embedded cluster metadata.

# Usage information
usage() {
	echo "Usage: $0 --cluster=IPv4 --benchmark=\"latency,bandwidth ...\" [--chunks_per_disk=N]"
	echo ""
	echo "Options:"
	echo "  --cluster=IPv4             IP address of the SaunaFS admin node"
	echo "  --benchmark=\"...\"        List of benchmarks, comma or space separated"
	echo "  --chunks_per_disk=N        Limit number of chunks to read per disk"
	echo "  -h, --help                 Show this help message and exit"
	echo "  -v, --verbose              Enable verbose output"
	exit 1
}

# Parse required arguments
CLUSTER_IP=""
BENCHMARKS=()
VERBOSE=0
CHUNKS_PER_DISK=""

# Check for --help/-h before parsing other args
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
	usage
fi

for arg in "$@"; do
	case $arg in
		--cluster=*)
			CLUSTER_IP="${arg#*=}"
			;;
		--benchmark=*)
			raw_benchmarks="${arg#*=}"
			# Replace commas with spaces and split into array
			IFS=' ' read -r -a BENCHMARKS <<< "${raw_benchmarks//,/ }"
			;;
		--chunks_per_disk=*)
			CHUNKS_PER_DISK="${arg#*=}"
			;;
		--verbose|-v)
			VERBOSE=1
			;;
		*)
			echo "Unknown argument: $arg" >&2
			exit 1
			;;
	esac
done

# Check for required arguments
if [[ -z "${CLUSTER_IP}" || ${#BENCHMARKS[@]} -eq 0 ]]; then
	usage
fi

if [[ ${VERBOSE} -eq 1 ]]; then
	echo "Cluster IP: ${CLUSTER_IP}" >&2
	echo "Benchmarks: ${BENCHMARKS[*]}" >&2
	[[ -n "${CHUNKS_PER_DISK}" ]] && echo "Chunks per disk: ${CHUNKS_PER_DISK}" >&2
	echo "Retrieving cluster chunk data using get_cluster.sh..." >&2
fi

# Build get_cluster.sh arguments
GET_CLUSTER_ARGS="--quiet"
[[ ${VERBOSE} -eq 1 ]] && GET_CLUSTER_ARGS=""
[[ -n "${CHUNKS_PER_DISK}" ]] && GET_CLUSTER_ARGS="${GET_CLUSTER_ARGS} --chunks_per_disk=${CHUNKS_PER_DISK}"

CLUSTER_JSON=$(./get_cluster.sh "${CLUSTER_IP}" ${GET_CLUSTER_ARGS})
if [[ -z "${CLUSTER_JSON}" ]]; then
	echo "Error: Failed to retrieve cluster JSON from get_cluster.sh" >&2
	exit 2
fi

# Generate benchmark JSONs
for BENCH in "${BENCHMARKS[@]}"; do
	[[ ${VERBOSE} -eq 1 ]] && echo "Generating JSON for benchmark: ${BENCH}" >&2
	jq -n --arg bench "${BENCH}" --argjson cluster "${CLUSTER_JSON}" \
		'{benchmark: $bench, cluster: $cluster}' > "benchmark_${BENCH}.json"
	echo "Generated benchmark_${BENCH}.json"
done
