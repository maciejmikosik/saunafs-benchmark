package com.saunafs.bm;

import com.saunafs.bm.model.Json;

public class Main {
  public static void main(String... args) {
    var json = new Json();
    var description = json.parse(System.in);

    switch (description.benchmark) {
      case "latency" -> new LatencyBenchmark().run(description);
      case "download" -> new DownloadBenchmark().run(description);
      default -> throw new IllegalArgumentException(
          "unknown benchmark: " + description.benchmark);
    }
    System.out.println(json.format(description));
  }
}
