package com.saunafs;

import static com.saunafs.Common.address;
import static com.saunafs.Common.socketAddress;
import static com.saunafs.ReadErasuredChunk.readErasuredChunk;
import static com.saunafs.Size.mebibytes;

import java.net.UnknownHostException;

public class ChunkBenchmarkExample2 {
  public static void main(String[] args) throws UnknownHostException {
    var address = socketAddress(address("192.168.168.96"), 9422);
    var server = new ChunkServer(address).connect();

    try {
      server.send(readErasuredChunk()
          .chunkId(502)
          .chunkVersion(1)
          .chunkType((short) 0)
          .offset(0)
          .size(mebibytes(64)));

      var response = (ReadStatus) server.receive();
      System.out.println(response);
    } finally {
      server.disconnect();
    }
  }
}
