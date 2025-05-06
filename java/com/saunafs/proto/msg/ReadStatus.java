package com.saunafs.proto.msg;

import com.saunafs.proto.Message;
import com.saunafs.proto.anno.Identifier;

@Identifier(code = 1201, version = 0)
public class ReadStatus implements Message {
  public long chunkId;
  public byte status;
}
