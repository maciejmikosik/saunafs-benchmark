package com.saunafs.proto.msg;

import com.saunafs.proto.Message;
import com.saunafs.proto.anno.Identifier;
import com.saunafs.proto.data.Size;

@Identifier(code = 1200, version = 1)
public class ReadErasuredChunk implements Message {
  public long chunkId;
  public int chunkVersion;
  public short chunkType;
  public int offset;
  public Size size;
}
