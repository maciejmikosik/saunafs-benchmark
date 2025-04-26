package com.saunafs.proto.msg;

import static com.saunafs.proto.Protocol.SAU_CSTOCL_READ_DATA;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.proto.Response;

public class ReadData implements Response {
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
    return "%s.version(%d).chunkId(%d).offset(%d).size(%d).crc(%d)"
        .formatted(
            SAU_CSTOCL_READ_DATA.identifier,
            SAU_CSTOCL_READ_DATA.version,
            chunkId,
            offset,
            size,
            crc);
  }
}
