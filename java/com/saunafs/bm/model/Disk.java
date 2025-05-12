package com.saunafs.bm.model;

import java.util.List;

public class Disk extends Model {
  public String name;
  public String location;
  public List<Chunk> chunks;
}
