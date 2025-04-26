package com.saunafs.proto.msg;

import static com.saunafs.proto.Description.description;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.proto.Description;
import com.saunafs.proto.Response;

public class ReadStatus implements Response {
  public static final Description description = description()
      .identifier("SAU_CSTOCL_READ_STATUS")
      .code(1201)
      .version(0)
      .decoder(ReadStatus::readStatus);

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
        description.identifier,
        description.version,
        chunkId,
        status);
  }
}
