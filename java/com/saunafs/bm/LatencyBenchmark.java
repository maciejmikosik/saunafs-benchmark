package com.saunafs.bm;

import static com.saunafs.bm.model.Helpers.countChunks;
import static com.saunafs.common.ProgressBar.progressBar;
import static com.saunafs.common.Timer.timer;
import static com.saunafs.common.io.InetServer.server;
import static com.saunafs.proto.data.Size.bytes;
import static com.saunafs.proto.msg.MessageBuilder.message;
import static com.saunafs.proto.msn.StreamingMessenger.streamingMessenger;
import static java.time.InstantSource.system;

import java.util.List;
import java.util.Map;

import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.proto.Messenger;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msg.ReadStatus;

public class LatencyBenchmark {
  public void run(List<ChunkServer> cluster) {
    var progressBar = progressBar().max(countChunks(cluster));

    for (var chunkServer : cluster) {
      var server = server(chunkServer.address);
      var messenger = streamingMessenger(server);
      try {
        server.connect();
        for (var disk : chunkServer.disks) {
          for (var chunk : disk.chunks) {
            benchmark(chunk, messenger);
            progressBar.increment();
          }
        }
      } finally {
        server.disconnect();
      }
    }
  }

  private void benchmark(Chunk chunk, Messenger messenger) {
    var message = message(ReadErasuredChunk.class)
        .chunkId(chunk.id)
        .chunkVersion(chunk.version)
        .chunkType(chunk.type)
        .size(bytes(1))
        .offset(0)
        .build();
    messenger.send(message);

    var timer = timer(system()).start();
    message = messenger.receive();
    if (message instanceof ReadData) {
      var time = timer.stop();
      while (message instanceof ReadData) {
        message = messenger.receive();
      }
      chunk.attachment = Map.of("latency", time);
    } else if (message instanceof ReadStatus readStatus) {
      chunk.attachment = Map.of("status", readStatus.status);
    } else {
      chunk.attachment = Map.of("error", "unknown message " + message.getClass().getSimpleName());
    }
  }
}
