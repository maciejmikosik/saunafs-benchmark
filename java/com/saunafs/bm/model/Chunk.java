package com.saunafs.bm.model;

import java.time.Duration;

import com.saunafs.proto.data.Size;

public class Chunk {
  public long id;
  public int version;
  public short type;
  public Size size;

  public Result result;

  public static class Result {
    public Duration time;
    public byte status;
  }
}
