package com.saunafs.bm.model;

import com.saunafs.proto.data.Size;

public class Chunk extends Model {
  public long id;
  public int version;
  public short type;
  public Size size;
}
