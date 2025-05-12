#!/bin/bash

# ------------------------------------------------------------------------------
# Generate a synthetic latency test file with multiple disk/device patterns.
# 
# Output format assumptions:
# - The file represents latency observations per disk per server.
# - Each block starts with a server line (e.g. /192.168.50.100:9422).
# - Then each disk is introduced by a path line (e.g. /mnt/data/ata-XYZ/).
# - Each latency entry is a pair of: sequence number and a latency value in seconds.
# - Format: <seq_num> <latency>s, indented with 4 spaces.
# 
# Patterns:
# - TRIANGLE: latency increases linearly from min to max.
# - SINUSOID: latency follows a sine wave pattern.
# - ZIGZAG: triangle wave pattern (sawtooth-style up/down every 10 steps).
# - MINMAX: alternating pairs of min and max values.
# 
# This format is compatible with latency graphing tools expecting the above layout.
# ------------------------------------------------------------------------------

out="/tmp/test_latency_patterns.01"
server="/192.168.50.100:9422"
count=400
min_latency=0.0001
max_latency=0.0020
mid_latency=0.0010
range_latency=$(awk -v min=${min_latency} -v max=${max_latency} 'BEGIN { print max - min }')
pi=3.14159

write_header() {
	echo "${server}" > "${out}"
}

write_pattern() {
	label="${1}"
	generator="${2}"
	start_index="${3}"
	echo "  /mnt/data/ata-${label}/" >> "${out}"
	seq 0 $((count - 1)) | pv -i 0.1 -l -s ${count} -N "${label}" | while read i; do
		latency=$(awk -v i=${i} -v pi=${pi} -v min=${min_latency} -v max=${max_latency} -v mid=${mid_latency} -v range=${range_latency} "${generator}")
		printf "    %7d    %s\n" $((start_index + i)) "${latency}" >> "${out}"
	done
}

triangle_generator=$(cat << 'EOF'
BEGIN {
	printf "%.6fs", min + i * range / (count - 1)
}
EOF
)

sinusoid_generator=$(cat << 'EOF'
BEGIN {
	a = sin(i / (count - 1) * pi * 2)
	printf "%.6fs", mid + 0.0009 * a
}
EOF
)

zigzag_generator=$(cat << 'EOF'
BEGIN {
	step = i % 20
	if (step < 10)
		printf "%.6fs", min + step * range / 9
	else
		printf "%.6fs", max - (step - 10) * range / 9
}
EOF
)

minmax_generator=$(cat << 'EOF'
BEGIN {
	v = int((i/2) % 2)
	printf "%.6fs", (v == 0 ? min : max)
}
EOF
)

write_header
write_pattern "TRIANGLE" "${triangle_generator}" 1000000
write_pattern "SINUSOID" "${sinusoid_generator}" 2000000
write_pattern "ZIGZAG" "${zigzag_generator}" 3000000
write_pattern "MINMAX" "${minmax_generator}" 4000000

chmod 644 "${out}"
echo "Generated ${out} with TRIANGLE, SINUSOID, ZIGZAG, and MINMAX patterns."

