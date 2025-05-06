package com.saunafs.proto.msg;

import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;

@Identifier(code = 1201, version = 0)
public class ReadStatus implements Message {
  public long chunkId;
  public byte status;
}
