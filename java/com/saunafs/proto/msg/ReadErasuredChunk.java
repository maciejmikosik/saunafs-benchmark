package com.saunafs.proto.msg;

import com.saunafs.common.Size;
import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;

@Identifier(code = 1200, version = 1)
public class ReadErasuredChunk implements Message {
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
}
