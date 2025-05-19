package com.saunafs.bm;

import static com.saunafs.bm.model.Cluster.formatCluster;
import static com.saunafs.bm.model.Cluster.parseCluster;

import java.io.InputStreamReader;

public class Main {
  public static void main(String... args) {
    var cluster = parseCluster(new InputStreamReader(System.in));
    new LatencyBenchmark().run(cluster);
    System.out.println(formatCluster(cluster));
  }
}
