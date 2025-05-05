package com.saunafs.proto.msg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.proto.Message;

public class ReadData implements Message {
  public static final int code = 1202;
  public int packetLength;
  public static final int version = 0;

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

  public String toString() {
    return "SAU_CSTOCL_READ_DATA.version(%d).chunkId(%d).offset(%d).size(%d).crc(%d)"
        .formatted(
            version,
            chunkId,
            offset,
            size,
            crc);
  }
}
