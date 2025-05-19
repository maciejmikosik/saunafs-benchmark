package com.saunafs.bm.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Json {
  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter())
      .registerTypeAdapter(Duration.class, new DurationAdapter())
      .setPrettyPrinting()
      .create();

  public Description parse(InputStream input) {
    return parse(new InputStreamReader(input));
  }

  public Description parse(Reader input) {
    return gson.fromJson(input, Description.class);
  }

  public String format(Description description) {
    return gson.toJson(description);
  }
}
