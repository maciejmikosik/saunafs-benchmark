package com.saunafs;

import static com.saunafs.Configuration.aNeutrinoGlobal;
import static com.saunafs.common.io.InetServer.server;
import static com.saunafs.proto.data.Size.mebibytes;
import static com.saunafs.proto.msg.MessageBuilder.message;
import static com.saunafs.proto.msn.StreamingMessenger.streamingMessenger;
import static com.saunafs.proto.msn.sniff.Sniffer.sniffer;
import static java.time.Clock.systemUTC;
import static java.time.Duration.between;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.saunafs.proto.Message;
import com.saunafs.proto.Messenger;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msn.sniff.Event;

public class Benchmark {
  public static void main(String... args) {
    var server = server(aNeutrinoGlobal);
    var events = new ArrayList<Event>();
    var sniffer = sniffer(events::add, systemUTC());
    var messenger = sniffer.sniff(streamingMessenger(server));
    try {
      server.connect();
      runSingleRead(messenger);
    } finally {
      server.disconnect();
    }
    print(events);
  }

  public static void runSingleRead(Messenger messenger) {
    var message = message(ReadErasuredChunk.class)
        .chunkId(0xC1)
        .chunkVersion(1)
        .chunkType((short) 0)
        .size(mebibytes(64))
        .offset(0)
        .build();
    messenger.send(message);
    while ((messenger.receive()) instanceof ReadData) {}
  }

  private static String format(Message message) {
    var simpleName = message.getClass().getSimpleName();
    var dataSize = switch (message) {
      case ReadData readData -> " %d".formatted(readData.blob.data.length);
      default -> "";
    };
    return simpleName + dataSize;
  }

  private static String format(Duration duration) {
    return "%3d.%03ds".formatted(
        duration.getSeconds(),
        duration.getNano() / 1_000_000);
  }

  private static void print(List<Event> events) {
    var firstEvent = events.get(0);
    for (Event event : events) {
      System.out.println("%8s %s %s".formatted(
          event.getClass().getSimpleName(),
          format(between(firstEvent.time, event.time)),
          format(event.message)));
    }
  }
}
