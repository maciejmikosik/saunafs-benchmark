package com.saunafs.proto.msg;

import static com.saunafs.proto.anno.Machine.CHUNK_SERVER;
import static com.saunafs.proto.anno.Machine.CLIENT;

import com.saunafs.proto.Message;
import com.saunafs.proto.anno.Direction;
import com.saunafs.proto.anno.Identifier;
import com.saunafs.proto.data.Size;

@Identifier(code = 1200, version = 1)
@Direction(from = CLIENT, to = CHUNK_SERVER)
public class ReadErasuredChunk implements Message {
  public long chunkId;
  public int chunkVersion;
  public short chunkType;
  public int offset;
  public Size size;
}
