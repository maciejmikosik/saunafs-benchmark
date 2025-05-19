package com.saunafs.bm;

import static com.saunafs.bm.model.Cluster.gson;
import static com.saunafs.bm.model.Cluster.parseCluster;
import static com.saunafs.common.ProgressBar.progressBar;
import static com.saunafs.common.io.InetServer.server;
import static com.saunafs.proto.data.Size.bytes;
import static com.saunafs.proto.msg.MessageBuilder.message;
import static com.saunafs.proto.msn.StreamingMessenger.streamingMessenger;
import static java.time.Duration.between;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Map;

import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Disk;
import com.saunafs.proto.Messenger;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msg.ReadStatus;

public class LatencyBenchmark {

  public static void main(String... args) {
    var cluster = parseCluster(new InputStreamReader(System.in));

    var nChunks = cluster.stream()
        .flatMap(chunkServer -> chunkServer.disks.stream())
        .flatMap(disk -> disk.chunks.stream())
        .count();
    var progressBar = progressBar().max(nChunks);

    for (ChunkServer chunkServer : cluster) {
      var server = server(chunkServer.address);
      var messenger = streamingMessenger(server);
      try {
        server.connect();
        for (Disk disk : chunkServer.disks) {
          for (Chunk chunk : disk.chunks) {
            benchmark(chunk, messenger);
            progressBar.increment();
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
        .chunkVersion(chunk.version)
        .chunkType(chunk.type)
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
