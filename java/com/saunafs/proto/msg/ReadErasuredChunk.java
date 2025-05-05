package com.saunafs.proto.msg;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.common.Size;
import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;

@Identifier(code = 1200, version = 1)
public class ReadErasuredChunk implements Message {
  public static final int packetLength = 26;

  public long chunkId;
  public int chunkVersion;
  public short chunkType;
  public int offset;
  public Size requestedSize;

  public static ReadErasuredChunk readErasuredChunk() {
    return new ReadErasuredChunk();
  }

  public ReadErasuredChunk chunkId(long chunkId) {
    this.chunkId = chunkId;
    return this;
  }

  public ReadErasuredChunk chunkVersion(int chunkVersion) {
    this.chunkVersion = chunkVersion;
    return this;
  }

  /** figure out how erasure coding works */
  public ReadErasuredChunk chunkType(short chunkType) {
    this.chunkType = chunkType;
    return this;
  }

  public ReadErasuredChunk offset(int offset) {
    this.offset = offset;
    return this;
  }

  public ReadErasuredChunk size(Size requestedSize) {
    this.requestedSize = requestedSize;
    return this;
  }

  public void writeTo(DataOutputStream output) {
    try {
      output.writeInt(1200);
      output.writeInt(packetLength);
      output.writeInt(1);
      output.writeLong(chunkId);
      output.writeInt(chunkVersion);
      output.writeShort(chunkType);
      output.writeInt(offset);
      output.writeInt(requestedSize.inBytes());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
