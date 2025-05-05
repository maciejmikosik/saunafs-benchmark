package com.saunafs.proto.msg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.proto.Message;

public class ReadStatus implements Message {
  public static final int code = 1201;
  public static final int packetLength = 13;
  public static final int version = 0;

  public long chunkId;
  public byte status;

  public static ReadStatus readStatus(DataInputStream input) {
    try {
      var response = new ReadStatus();
      response.chunkId = input.readLong();
      response.status = input.readByte();
      return response;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String toString() {
    return "SAU_CSTOCL_READ_STATUS.version(%d).chunkId(%d).status(%d)".formatted(
        version,
        chunkId,
        status);
  }
}
