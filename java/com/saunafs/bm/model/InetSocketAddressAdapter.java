package com.saunafs.bm.model;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class InetSocketAddressAdapter implements
    JsonSerializer<InetSocketAddress>,
    JsonDeserializer<InetSocketAddress> {
  public JsonElement serialize(
      InetSocketAddress model,
      Type type,
      JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("ip", model.getHostString());
    jsonObject.addProperty("port", model.getPort());
    return jsonObject;
  }

  public InetSocketAddress deserialize(
      JsonElement jsonElement,
      Type type,
      JsonDeserializationContext context) {
    if (jsonElement.isJsonObject()) {
      var jsonObject = jsonElement.getAsJsonObject();
      return new InetSocketAddress(
          jsonObject.get("ip").getAsString(),
          jsonObject.get("port").getAsInt());
    } else {
      throw new JsonParseException("cannot deserialize InetSocketAddress");
    }
  }
}
