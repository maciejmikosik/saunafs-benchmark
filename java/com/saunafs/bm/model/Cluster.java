package com.saunafs.bm.model;

import java.io.Reader;
import java.net.InetSocketAddress;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Cluster {
  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter())
      .setPrettyPrinting()
      .create();

  public static List<ChunkServer> parseCluster(Reader input) {
    var type = new TypeToken<List<ChunkServer>>() {}.getType();
    return gson.fromJson(input, type);
  }

  public static String formatCluster(List<ChunkServer> cluster) {
    return gson.toJson(cluster);
  }
}
