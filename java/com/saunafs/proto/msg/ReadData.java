package com.saunafs.proto.msg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;

@Identifier(code = 1202, version = 0)
public class ReadData implements Message {
  public int packetLength;

  public long chunkId;
  public int offset;
  public int size;
  public int crc;
  public byte[] data;

  public static ReadData readData(DataInputStream input) {
    try {
      // TODO initialize packetLength
      var response = new ReadData();
      response.chunkId = input.readLong();
      response.offset = input.readInt();
      response.size = input.readInt();
      response.crc = input.readInt();
      response.data = input.readNBytes(response.size);
      return response;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
