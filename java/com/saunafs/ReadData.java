package com.saunafs;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ReadData implements Response {
  public static final String messageName = "SAU_CSTOCL_READ_DATA";
  public static final int messageType = 1202;
  public static final int version = 0;

  public long chunkId;
  public int offset;
  public int size;
  public int crc;
  public byte[] data;

  public static ReadData readData(DataInputStream input) {
    try {
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
    return "readData.version(%d).chunkId(%d).offset(%d).size(%d).crc(%d)"
        .formatted(version, chunkId, offset, size, crc);
  }
}
