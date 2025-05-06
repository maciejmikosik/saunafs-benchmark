package com.saunafs.proto.msg;

import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;
import com.saunafs.proto.data.Blob;

@Identifier(code = 1202, version = 0)
public class ReadData implements Message {
  public long chunkId;
  public int offset;
  public Blob blob;
}
