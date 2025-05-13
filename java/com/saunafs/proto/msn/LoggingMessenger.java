package com.saunafs.proto.msn;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.PrintStream;
import java.lang.reflect.Field;

import com.saunafs.proto.Message;
import com.saunafs.proto.Messenger;
import com.saunafs.proto.data.Blob;
import com.saunafs.proto.data.Size;

public class LoggingMessenger implements Messenger {
  private static final PrintStream log = System.out;
  private final Messenger messenger;

  private LoggingMessenger(Messenger messenger) {
    this.messenger = messenger;
  }

  public static Messenger logging(Messenger messenger) {
    return new LoggingMessenger(messenger);
  }

  public void send(Message message) {
    log.print("-> ");
    log.print(format(message));
    messenger.send(message);
    log.println(" ✓");
  }

  public Message receive() {
    log.print("<- ");
    var message = messenger.receive();
    log.print(format(message));
    log.println(" ✓");
    return message;
  }

  private static String format(Message message) {
    return message.getClass().getSimpleName() + formatFieldsOf(message);
  }

  private static String formatFieldsOf(Object object) {
    return stream(object.getClass().getDeclaredFields())
        .map(field -> format(field, object))
        .collect(joining(""));
  }

  private static String format(Field field, Object object) {
    try {
      var name = field.getName();
      var value = field.get(object);
      return switch (value) {
        case Size size -> ".%s(%d)".formatted(name, size.inBytes());
        case Blob blob -> format(blob);
        default -> ".%s(%s)".formatted(name, value);
      };
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static String format(Blob blob) {
    return ".size(%d).crc(%d)"
        .formatted(blob.data.length, blob.crc);
  }
}
