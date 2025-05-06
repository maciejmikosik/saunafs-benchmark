package com.saunafs.server;

import static com.saunafs.common.Common.read;
import static com.saunafs.proto.Protocol.decoder;
import static com.saunafs.proto.Protocol.messageClass;
import static com.saunafs.proto.Protocol.packetLengthFor;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;

import com.saunafs.common.Size;
import com.saunafs.proto.Identifier;
import com.saunafs.proto.Message;

public class StreamingMessenger implements Messenger {
  private final DataOutputStream output;
  private final DataInputStream input;

  private StreamingMessenger(DataOutputStream output, DataInputStream input) {
    this.output = output;
    this.input = input;
  }

  public static Messenger streamingMessenger(Server server) {
    return new StreamingMessenger(
        new DataOutputStream(server.output()),
        new DataInputStream(server.input()));
  }

  public void send(Message message) {
    try {
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
            .forEach(field -> write(read(field, object)));
        default -> throw new RuntimeException("cannot serialize: " + object);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public Message receive() {
    try {
      var code = input.readInt();
      @SuppressWarnings("unused")
      var length = input.readInt();
      var version = input.readInt();

      // TODO implement lookup table
      Method decoder = decoder(messageClass(code, version));
      return (Message) decoder.invoke(null, input);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
