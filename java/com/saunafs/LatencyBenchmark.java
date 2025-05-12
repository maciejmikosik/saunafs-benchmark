package com.saunafs;

import static com.saunafs.bm.model.Cluster.parseCluster;
import static com.saunafs.common.Collections.toMapFromEntries;
import static com.saunafs.proto.MessageBuilder.message;
import static com.saunafs.proto.data.Size.bytes;
import static com.saunafs.server.InetServer.server;
import static com.saunafs.server.StreamingMessenger.streamingMessenger;
import static java.time.Duration.between;
import static java.util.Map.entry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Disk;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msg.ReadStatus;
import com.saunafs.server.Messenger;

public class LatencyBenchmark {
  public static void main(String... args) throws IOException {
    var cluster = parseCluster(new InputStreamReader(System.in));
    var analytics = cluster.stream()
        .map(chunkServer -> Map.entry(chunkServer, benchmark(chunkServer)))
        .collect(toMapFromEntries());

    for (ChunkServer chunkServer : cluster) {
      println(chunkServer.address);
      for (Disk disk : chunkServer.disks) {
        print("  " + disk.location);
        var durations = analytics.get(chunkServer).get(disk);
        println("  %s %s %s".formatted(
            format(durations.getFirst()),
            format(durations.get(durations.size() / 2)),
            format(durations.getLast())));
      }
    }
  }

  private static Map<Disk, List<Duration>> benchmark(ChunkServer chunkServer) {
    println(chunkServer.address);
    var server = server(chunkServer.address);
    var messenger = streamingMessenger(server);
    try {
      server.connect();
      return chunkServer.disks.stream()
          .map(disk -> entry(disk, benchmark(disk, messenger)))
          .collect(toMapFromEntries());
    } finally {
      server.disconnect();
    }
  }

  private static List<Duration> benchmark(Disk disk, Messenger messenger) {
    println("  " + disk.location);
    return disk.chunks.stream()
        .map(chunkId -> benchmark(chunkId, messenger))
        .flatMap(Optional::stream)
        .sorted()
        .toList();
  }

  private static Optional<Duration> benchmark(Long chunkId, Messenger messenger) {
    print("    " + chunkId + "  ");
    var message = message(ReadErasuredChunk.class)
        .chunkId(chunkId)
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
      var duration = between(start, stop);
      println(format(duration));
      return Optional.of(duration);
    } else if (message instanceof ReadStatus readStatus) {
      println("status=" + readStatus.status);
    } else {
      println("unexpected message " + message);
    }
    return Optional.empty();
  }

  private static String format(Duration duration) {
    return "%3d.%09ds".formatted(
        duration.getSeconds(),
        duration.getNano());
  }

  private static void print(Object object) {
    System.out.print(object);
  }

  private static void println(Object object) {
    System.out.println(object);
  }
}
