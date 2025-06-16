package com.saunafs.bm.html.model;

import java.time.Duration;

import com.saunafs.common.quant.Size;
import com.saunafs.proto.data.Status;

public class Transfer {
  public String item;
  public Status status;
  public Size size;
  public Duration duration;
}
