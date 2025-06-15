package com.saunafs.bm.model;

import java.util.List;

public class Helpers {
  public static long countChunks(List<ChunkServer> cluster) {
    return cluster.stream()
        .flatMap(chunkServer -> chunkServer.disks.stream())
        .flatMap(disk -> disk.chunks.stream())
        .count();
  }

  public static boolean isSuccessful(Chunk chunk) {
    return chunk.result.status == 0;
  }

  public static List<Chunk> filterSuccessful(List<Chunk> chunks) {
    return chunks.stream()
        .filter(Helpers::isSuccessful)
        .toList();
  }
}
