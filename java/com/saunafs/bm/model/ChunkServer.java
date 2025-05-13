package com.saunafs.bm.model;

import java.net.InetSocketAddress;
import java.util.List;

public class ChunkServer extends Model {
  public InetSocketAddress address;
  public List<Disk> disks;
}
