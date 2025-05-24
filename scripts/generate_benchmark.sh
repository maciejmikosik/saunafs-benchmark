#!/bin/bash

# Description:
# This script generates benchmark-specific JSON documents using cluster data
# retrieved from the get_cluster.sh script. Each output includes the benchmark
# type and embedded cluster metadata.

# Parse required arguments
CLUSTER_IP=""
BENCHMARKS=()

for arg in "$@"; do
	case $arg in
		--cluster=*)
			CLUSTER_IP="${arg#*=}"
			;;
		--benchmark=*)
			IFS=' ' read -r -a BENCHMARKS <<< "${arg#*=}"
			;;
		*)
			echo "Unknown argument: $arg" >&2
			exit 1
			;;
	esac
done

# Check for required arguments
if [[ -z "${CLUSTER_IP}" || ${#BENCHMARKS[@]} -eq 0 ]]; then
	echo "Usage: $0 --cluster=IPv4 --benchmark=\"latency bandwidth ...\"" >&2
	exit 1
fi

# Fetch cluster JSON from get_cluster.sh
CLUSTER_JSON=$(./get_cluster.sh "$CLUSTER_IP" --quiet)
if [[ -z "${CLUSTER_JSON}" ]]; then
	echo "Error: Failed to retrieve cluster JSON from get_cluster.sh" >&2
	exit 2
fi

# Generate benchmark JSONs
for BENCH in "${BENCHMARKS[@]}"; do
	jq -n --arg bench "${BENCH}" --argjson cluster "${CLUSTER_JSON}" \
		'{benchmark: $bench, cluster: $cluster}' > "benchmark_${BENCH}.json"
	echo "Generated benchmark_${BENCH}.json"
done

