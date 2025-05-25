package com.saunafs.bm.model.gson;

import java.lang.reflect.Type;
import java.time.Instant;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class InstantAdapter implements
    JsonSerializer<Instant>,
    JsonDeserializer<Instant> {
  public JsonElement serialize(
      Instant instant,
      Type type,
      JsonSerializationContext context) {
    return new JsonPrimitive(instant.toString());
  }

  public Instant deserialize(
      JsonElement jsonElement,
      Type type,
      JsonDeserializationContext context)
      throws JsonParseException {
    return Instant.parse(jsonElement.getAsString());
  }
}
