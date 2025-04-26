package com.saunafs;

import static com.saunafs.common.Common.address;
import static com.saunafs.common.Common.socketAddress;
import static com.saunafs.common.Size.mebibytes;
import static com.saunafs.proto.msg.ReadErasuredChunk.readErasuredChunk;

import com.saunafs.proto.Message;
import com.saunafs.proto.msg.ReadData;

/**
 * saunfs protocol constants
 * https://github.com/leil-io/saunafs/blob/dev/src/protocol/SFSCommunication.h
 *
 * other benchmark constants
 * https://github.com/leil-io/saunafs-sandbox/blob/dev/chunk-benchmark/src/protocol_consts.h
 */
public class Demo {
  public static void main(String... args) {
    var address = socketAddress(address("192.168.168.160"), 9422);
    var server = new ChunkServer(address).connect();
    try {
      demo(server);
    } finally {
      server.disconnect();
    }
  }

  private static void demo(ChunkServer server) {
    server.send(readErasuredChunk()
        .chunkId(0xC1)
        .chunkVersion(1)
        .chunkType((short) 0)
        .offset(0)
        .size(mebibytes(64)));

    Message message;
    while ((message = server.receive()) instanceof ReadData readData) {
      System.out.println(readData);
    }
    System.out.println(message);
  }
}
