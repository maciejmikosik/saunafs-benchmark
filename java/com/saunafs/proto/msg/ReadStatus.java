package com.saunafs.proto.msg;

import static com.saunafs.proto.anno.Machine.CHUNK_SERVER;
import static com.saunafs.proto.anno.Machine.CLIENT;

import com.saunafs.proto.Message;
import com.saunafs.proto.anno.Direction;
import com.saunafs.proto.anno.Identifier;

@Identifier(code = 1201, version = 0)
@Direction(from = CHUNK_SERVER, to = CLIENT)
public class ReadStatus implements Message {
  public long chunkId;
  public byte status;
}
