package com.saunafs.proto.msg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.common.Blob;
import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;

@Identifier(code = 1202, version = 0)
public class ReadData implements Message {
  public long chunkId;
  public int offset;
  public Blob blob;

  public static ReadData readData(DataInputStream input) {
    try {
      var response = new ReadData();
      response.chunkId = input.readLong();
      response.offset = input.readInt();
      response.blob = new Blob();
      var size = input.readInt();
      response.blob.crc = input.readInt();
      response.blob.data = input.readNBytes(size);
      return response;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
