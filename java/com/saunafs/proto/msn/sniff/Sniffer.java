package com.saunafs.proto.msn.sniff;

import static com.saunafs.proto.msn.sniff.Received.received;
import static com.saunafs.proto.msn.sniff.Sent.sent;

import java.time.InstantSource;
import java.util.function.Consumer;

import com.saunafs.proto.Message;
import com.saunafs.proto.Messenger;

public class Sniffer {
  private final Consumer<Event> log;
  private final InstantSource clock;

  private Sniffer(Consumer<Event> log, InstantSource clock) {
    this.log = log;
    this.clock = clock;
  }

  public static Sniffer sniffer(Consumer<Event> log, InstantSource clock) {
    return new Sniffer(log, clock);
  }

  public Messenger sniff(Messenger messenger) {
    return new Messenger() {
      public void send(Message message) {
        messenger.send(message);
        log.accept(sent(message, clock.instant()));
      }

      public Message receive() {
        var message = messenger.receive();
        log.accept(received(message, clock.instant()));
        return message;
      }
    };
  }
}
