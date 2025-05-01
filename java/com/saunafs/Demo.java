package com.saunafs;

import static com.saunafs.Configuration.aNeutrinoGlobal;
import static com.saunafs.common.Size.mebibytes;
import static com.saunafs.proto.msg.ReadErasuredChunk.readErasuredChunk;
import static com.saunafs.server.InetServer.server;
import static com.saunafs.server.StreamingMessenger.streamingMessenger;

import com.saunafs.proto.Message;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.server.Messenger;

/**
 * saunfs protocol constants
 * https://github.com/leil-io/saunafs/blob/dev/src/protocol/SFSCommunication.h
 *
 * other benchmark constants
 * https://github.com/leil-io/saunafs-sandbox/blob/dev/chunk-benchmark/src/protocol_consts.h
 */
public class Demo {
  public static void main(String... args) {
    var server = server(aNeutrinoGlobal);
    server.connect();
    try {
      demo(streamingMessenger(server));
    } finally {
      server.disconnect();
    }
  }

  private static void demo(Messenger messenger) {
    messenger.send(readErasuredChunk()
        .chunkId(0xC1)
        .chunkVersion(1)
        .chunkType((short) 0)
        .offset(0)
        .size(mebibytes(64)));

    Message message;
    while ((message = messenger.receive()) instanceof ReadData readData) {
      System.out.println(readData);
    }
    System.out.println(message);
  }
}
