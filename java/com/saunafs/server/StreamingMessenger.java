package com.saunafs.server;

import static com.saunafs.proto.Protocol.messageClass;
import static com.saunafs.proto.Protocol.packetLengthFor;
import static com.saunafs.proto.data.Size.bytes;
import static java.lang.reflect.Modifier.isStatic;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;

import com.saunafs.proto.Message;
import com.saunafs.proto.anno.Identifier;
import com.saunafs.proto.data.Blob;
import com.saunafs.proto.data.Size;

public class StreamingMessenger implements Messenger {
  private final Server server;
  private DataOutputStream output;
  private DataInputStream input;

  private StreamingMessenger(Server server) {
    this.server = server;
  }

  public static Messenger streamingMessenger(Server server) {
    return new StreamingMessenger(server);
  }

  public void send(Message message) {
    try {
      output = new DataOutputStream(new BufferedOutputStream(server.output()));
      var identifier = message.getClass().getAnnotation(Identifier.class);
      write(identifier.code());
      write(packetLengthFor(message));
      write(identifier.version());
      write(message);
      output.flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void write(Object object) throws IOException {
    switch (object) {
      case Byte number -> output.writeByte(number);
      case Short number -> output.writeShort(number);
      case Integer number -> output.writeInt(number);
      case Long number -> output.writeLong(number);
      case Size size -> output.writeInt(size.inBytes());
      case Message message -> writeReflectively(message);
      default -> throw new RuntimeException("cannot serialize: " + object);
    }
  }

  private void writeReflectively(Object instance) throws IOException {
    try {
      for (Field field : instance.getClass().getDeclaredFields()) {
        if (!isStatic(field.getModifiers())) {
          write(field.get(instance));
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public Message receive() {
    try {
      input = new DataInputStream(server.input());
      var code = input.readInt();
      @SuppressWarnings("unused")
      var length = input.readInt();
      var version = input.readInt();
      return (Message) read(messageClass(code, version));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Object read(Class<?> type) throws IOException {
    if (type == byte.class) {
      return input.readByte();
    } else if (type == short.class) {
      return input.readShort();
    } else if (type == int.class) {
      return input.readInt();
    } else if (type == long.class) {
      return input.readLong();
    } else if (type == Size.class) {
      return bytes(input.readInt());
    } else if (type == Blob.class) {
      var blob = new Blob();
      var size = input.readInt();
      blob.crc = input.readInt();
      blob.data = input.readNBytes(size);
      return blob;
    } else if (Message.class.isAssignableFrom(type)) {
      return readReflectively(type);
    } else {
      throw new RuntimeException("cannot deserialize " + type);
    }
  }

  private Object readReflectively(Class<?> type) throws IOException {
    try {
      var instance = type.getDeclaredConstructor().newInstance();
      for (Field field : type.getDeclaredFields()) {
        if (!isStatic(field.getModifiers())) {
          field.set(instance, read(field.getType()));
        }
      }
      return instance;
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
