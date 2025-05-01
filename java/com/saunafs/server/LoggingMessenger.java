package com.saunafs.server;

import java.io.PrintStream;

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
    log.print(message);
    messenger.send(message);
    log.println(" ✓");
  }

  public Message receive() {
    log.print("<- ");
    var message = messenger.receive();
    log.print(message);
    log.println(" ✓");
    return message;
  }
}
