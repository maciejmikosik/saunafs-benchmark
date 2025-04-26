package com.saunafs.proto.msg;

import static com.saunafs.proto.Protocol.SAU_CSTOCL_READ_STATUS;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.proto.Response;

public class ReadStatus implements Response {
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
    return "%s.version(%d).chunkId(%d).status(%d)".formatted(
        SAU_CSTOCL_READ_STATUS.identifier,
        SAU_CSTOCL_READ_STATUS.version,
        chunkId,
        status);
  }
}
