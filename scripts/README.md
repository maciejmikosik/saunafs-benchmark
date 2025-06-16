example running benchmark
- for 192.168.50.199
- and only 3 chunks per disk
- two benchmarks latency and download

./generate_benchmark.sh --verbose --size=$((65536)) --saunafs_cluster_ip=192.168.50.199 --chunks_per_disk=3 --benchmark="latency download" | while read foo benchmark ; do  echo ${benchmark} ; cat ${benchmark} | /usr/bin/time -v benchmark/run.sh > ${benchmark}.results.json ; done
