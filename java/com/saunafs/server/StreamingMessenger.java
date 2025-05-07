package com.saunafs.server;

import static com.saunafs.common.Common.set;
import static com.saunafs.proto.Protocol.messageClass;
import static com.saunafs.proto.Protocol.packetLengthFor;
import static com.saunafs.proto.data.Size.bytes;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.common.Common;
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
      output = new DataOutputStream(server.output());
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

  private void write(Object object) {
    try {
      switch (object) {
        case Byte number -> output.writeByte(number);
        case Short number -> output.writeShort(number);
        case Integer number -> output.writeInt(number);
        case Long number -> output.writeLong(number);
        case Size size -> output.writeInt(size.inBytes());
        case Message message -> stream(message.getClass().getDeclaredFields())
            .filter(field -> !isStatic(field.getModifiers()))
            .forEach(field -> write(Common.read(field, object)));
        default -> throw new RuntimeException("cannot serialize: " + object);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
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

  private Object read(Class<?> type) {
    try {
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
        blob.data = new byte[size];
        var count = 0;
        while (count < size) {
          count += input.read(blob.data, count, size - count);
        }
        return blob;
      } else if (Message.class.isAssignableFrom(type)) {
        var message = type.getDeclaredConstructor().newInstance();
        stream(type.getDeclaredFields())
            .filter(field -> !isStatic(field.getModifiers()))
            .forEach(field -> set(field, message, read(field.getType())));
        return message;
      } else {
        throw new RuntimeException("cannot deserialize " + type);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
