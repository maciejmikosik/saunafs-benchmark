package com.saunafs.proto.msg;

import com.saunafs.common.Blob;
import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;

@Identifier(code = 1202, version = 0)
public class ReadData implements Message {
  public long chunkId;
  public int offset;
  public Blob blob;
}
