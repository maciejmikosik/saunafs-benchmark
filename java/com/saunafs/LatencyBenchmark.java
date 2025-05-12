package com.saunafs;

import static com.saunafs.bm.model.Cluster.gson;
import static com.saunafs.bm.model.Cluster.parseCluster;
import static com.saunafs.common.ProgressBar.progressBar;
import static com.saunafs.proto.MessageBuilder.message;
import static com.saunafs.proto.data.Size.bytes;
import static com.saunafs.server.InetServer.server;
import static com.saunafs.server.StreamingMessenger.streamingMessenger;
import static java.time.Duration.between;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Map;

import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Disk;
import com.saunafs.common.ProgressBar;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msg.ReadStatus;
import com.saunafs.server.Messenger;

public class LatencyBenchmark {
  private static final ProgressBar progressBar = progressBar();

  public static void main(String... args) throws IOException {
    var cluster = parseCluster(new InputStreamReader(System.in));

    var nChunks = cluster.stream()
        .flatMap(chunkServer -> chunkServer.disks.stream())
        .flatMap(disk -> disk.chunks.stream())
        .count();
    var nChunksChecked = 0;

    for (ChunkServer chunkServer : cluster) {
      var server = server(chunkServer.address);
      var messenger = streamingMessenger(server);
      try {
        server.connect();
        for (Disk disk : chunkServer.disks) {
          for (Chunk chunk : disk.chunks) {
            benchmark(chunk, messenger);
            progressBar.update(1f * (++nChunksChecked) / nChunks);
          }
        }
      } finally {
        server.disconnect();
      }
    }

    System.out.println(gson.toJson(cluster));
  }

  private static void benchmark(Chunk chunk, Messenger messenger) {
    var message = message(ReadErasuredChunk.class)
        .chunkId(chunk.id)
        .chunkVersion(1)
        .chunkType((short) 0)
        .size(bytes(1))
        .offset(0)
        .build();
    messenger.send(message);

    var start = Instant.now();
    message = messenger.receive();
    if (message instanceof ReadData) {
      var stop = Instant.now();
      while (message instanceof ReadData) {
        message = messenger.receive();
      }
      chunk.attachment = Map.of("latency", between(start, stop));
    } else if (message instanceof ReadStatus readStatus) {
      chunk.attachment = Map.of("status", readStatus.status);
    } else {
      chunk.attachment = Map.of("error", "unknown message " + message.getClass().getSimpleName());
    }
  }
}
