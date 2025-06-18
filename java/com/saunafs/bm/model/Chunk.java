package com.saunafs.bm.model;

import com.saunafs.common.Interval;
import com.saunafs.common.quant.Size;
import com.saunafs.proto.data.Status;

public class Chunk {
  public long id;
  public int version;
  public short type;
  public Size size;

  public Result result;

  public static class Result {
    public Interval time;
    public Status status;
  }
}
