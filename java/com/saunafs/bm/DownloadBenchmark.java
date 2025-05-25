package com.saunafs.bm;

import static com.saunafs.bm.model.Helpers.countChunks;
import static com.saunafs.common.ProgressBar.progressBar;
import static com.saunafs.common.Timer.timer;
import static com.saunafs.common.io.InetServer.server;
import static com.saunafs.proto.msg.MessageBuilder.message;
import static com.saunafs.proto.msn.StreamingMessenger.streamingMessenger;
import static java.time.InstantSource.system;

import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.Description;
import com.saunafs.proto.Messenger;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msg.ReadStatus;

public class DownloadBenchmark {
  public void run(Description description) {
    var progressBar = progressBar().max(countChunks(description.cluster));

    for (var chunkServer : description.cluster) {
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
        .size(chunk.size)
        .offset(0)
        .build();
    messenger.send(message);

    var timer = timer(system()).start();
    do {
      message = messenger.receive();
    } while (message instanceof ReadData);
    chunk.result = new Chunk.Result();
    chunk.result.time = timer.stop();
    chunk.result.status = ((ReadStatus) message).status;
  }
}
