package com.saunafs.bm.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.saunafs.bm.model.gson.DurationAdapter;
import com.saunafs.bm.model.gson.InetSocketAddressAdapter;
import com.saunafs.bm.model.gson.InstantAdapter;
import com.saunafs.bm.model.gson.SizeAdapter;
import com.saunafs.proto.data.Size;

public class Json {
  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter())
      .registerTypeAdapter(Instant.class, new InstantAdapter())
      .registerTypeAdapter(Duration.class, new DurationAdapter())
      .registerTypeAdapter(Size.class, new SizeAdapter())
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
