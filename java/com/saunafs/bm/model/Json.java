package com.saunafs.bm.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Json {
  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter())
      .registerTypeAdapter(Duration.class, new DurationAdapter())
      .setPrettyPrinting()
      .create();

  public List<ChunkServer> parse(InputStream input) {
    return parse(new InputStreamReader(input));
  }

  public List<ChunkServer> parse(Reader input) {
    var type = new TypeToken<List<ChunkServer>>() {}.getType();
    return gson.fromJson(input, type);
  }

  public String format(List<ChunkServer> cluster) {
    return gson.toJson(cluster);
  }
}
