package com.saunafs.bm.model.gson;

import static com.saunafs.proto.data.Size.bytes;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.saunafs.proto.data.Size;

public class SizeAdapter implements
    JsonSerializer<Size>,
    JsonDeserializer<Size> {
  public JsonElement serialize(
      Size model,
      Type type,
      JsonSerializationContext context) {
    return new JsonPrimitive(model.inBytes());
  }

  public Size deserialize(
      JsonElement jsonElement,
      Type type,
      JsonDeserializationContext context) {
    if (jsonElement.isJsonPrimitive()) {
      return bytes(jsonElement.getAsJsonPrimitive().getAsInt());
    } else {
      throw new JsonParseException("cannot deserialize Size");
    }
  }
}
