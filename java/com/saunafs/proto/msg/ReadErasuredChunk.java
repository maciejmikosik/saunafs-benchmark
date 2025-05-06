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
  public Size size;
}
