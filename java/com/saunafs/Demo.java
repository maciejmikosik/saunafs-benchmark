package com.saunafs;

import static com.saunafs.Configuration.aNeutrinoGlobal;
import static com.saunafs.bm.Sniffer.sniffer;
import static com.saunafs.proto.MessageBuilder.message;
import static com.saunafs.proto.data.Size.mebibytes;
import static com.saunafs.server.InetServer.server;
import static com.saunafs.server.LoggingMessenger.logging;
import static com.saunafs.server.StreamingMessenger.streamingMessenger;
import static java.time.Clock.systemUTC;

import java.util.ArrayList;

import com.saunafs.bm.Event;
import com.saunafs.bm.Received;
import com.saunafs.bm.Sent;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.server.Messenger;

/**
 * saunfs protocol constants
 * https://github.com/leil-io/saunafs/blob/dev/src/protocol/SFSCommunication.h
 *
 * other benchmark constants
 * https://github.com/leil-io/saunafs-sandbox/blob/dev/chunk-benchmark/src/protocol_consts.h
 */
public class Demo {
  public static void main(String... args) {
    var server = server(aNeutrinoGlobal);
    var events = new ArrayList<Event>();
    var sniffer = sniffer(events::add, systemUTC());
    try {
      server.connect();
      var messenger = logging(sniffer.sniff(streamingMessenger(server)));
      demo(messenger);
    } finally {
      server.disconnect();
    }

    for (Event event : events) {
      System.out.println(format(event));
    }
  }

  private static String format(Event event) {
    return switch (event) {
      case Sent sent -> {
        yield "    sent %s %s".formatted(
            sent.time,
            sent.message.getClass().getSimpleName());
      }
      case Received received -> {
        yield "received %s %s".formatted(
            received.time,
            received.message.getClass().getSimpleName());
      }
      default -> throw new RuntimeException();
    };
  }

  private static void demo(Messenger messenger) {
    messenger.send(message(ReadErasuredChunk.class)
        .chunkId(0xC1)
        .chunkVersion(1)
        .chunkType((short) 0)
        .offset(0)
        .size(mebibytes(64))
        .build());

    while (messenger.receive() instanceof ReadData) {}
  }
}
