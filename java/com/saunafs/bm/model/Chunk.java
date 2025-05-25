package com.saunafs.bm.model;

import com.saunafs.common.Interval;
import com.saunafs.proto.data.Size;

public class Chunk {
  public long id;
  public int version;
  public short type;
  public Size size;

  public Result result;

  public static class Result {
    public Interval time;
    public byte status;
  }
}
