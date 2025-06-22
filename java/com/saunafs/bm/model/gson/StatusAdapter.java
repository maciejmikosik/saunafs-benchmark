package com.saunafs.bm.model.gson;

import static com.saunafs.proto.data.Status.status;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.saunafs.proto.data.Status;

public class StatusAdapter implements
    JsonSerializer<Status>,
    JsonDeserializer<Status> {
  public JsonElement serialize(
      Status status,
      Type type,
      JsonSerializationContext context) {
    return new JsonPrimitive(status.code);
  }

  public Status deserialize(
      JsonElement jsonElement,
      Type type,
      JsonDeserializationContext context)
      throws JsonParseException {
    return status(jsonElement.getAsByte());
  }
}
