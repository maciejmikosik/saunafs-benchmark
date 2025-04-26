package com.saunafs;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ReadStatus implements Response {
  public static final String messageName = "SAU_CSTOCL_READ_STATUS";
  public static final int messageType = 1201;

  public int version;
  public long chunkId;
  public byte status;

  public static ReadStatus readStatus(DataInputStream input) {
    try {
      var response = new ReadStatus();
      response.version = input.readInt();
      response.chunkId = input.readLong();
      response.status = input.readByte();
      return response;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String toString() {
    return "readStatus.version(%d).chunkId(%d).status(%d)".formatted(
        version, chunkId, status);
  }
}
