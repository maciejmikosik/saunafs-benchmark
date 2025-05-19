package com.saunafs.bm;

import com.saunafs.bm.model.Json;

public class Main {
  public static void main(String... args) {
    var json = new Json();
    var cluster = json.parse(System.in);
    new LatencyBenchmark().run(cluster);
    System.out.println(json.format(cluster));
  }
}
