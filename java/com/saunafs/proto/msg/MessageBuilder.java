package com.saunafs.proto.msg;

import com.saunafs.proto.Message;
import com.saunafs.proto.data.Size;

public class MessageBuilder {
  private final Message message;

  private MessageBuilder(Message message) {
    this.message = message;
  }

  public static MessageBuilder message(Class<? extends Message> type) {
    try {
      return new MessageBuilder(type.getDeclaredConstructor().newInstance());
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public MessageBuilder setField(String name, Object value) {
    try {
      message.getClass()
          .getDeclaredField(name)
          .set(message, value);
      return this;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public Message build() {
    return message;
  }

  public MessageBuilder chunkId(long chunkId) {
    return setField("chunkId", chunkId);
  }

  public MessageBuilder chunkVersion(int chunkVersion) {
    return setField("chunkVersion", chunkVersion);
  }

  public MessageBuilder chunkType(short chunkType) {
    return setField("chunkType", chunkType);
  }

  public MessageBuilder offset(int offset) {
    return setField("offset", offset);
  }

  public MessageBuilder size(Size size) {
    return setField("size", size);
  }
}
