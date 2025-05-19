package com.saunafs.bm.model;

import java.util.List;

public class Helpers {
  public static long countChunks(List<ChunkServer> cluster) {
    return cluster.stream()
        .flatMap(chunkServer -> chunkServer.disks.stream())
        .flatMap(disk -> disk.chunks.stream())
        .count();
  }
}
