package com.saunafs.server;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.PrintStream;
import java.lang.reflect.Field;

import com.saunafs.proto.Message;

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
    var messageClass = message.getClass();
    return stream(message.getClass().getDeclaredFields())
        .map(field -> format(field, message))
        .collect(joining("", messageClass.getSimpleName(), ""));
  }

  private static String format(Field field, Message message) {
    try {
      return ".%s(%s)".formatted(
          field.getName(),
          field.get(message));
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
