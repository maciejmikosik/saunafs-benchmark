package com.saunafs.proto.msg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;

@Identifier(code = 1201, version = 0)
public class ReadStatus implements Message {
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
}
