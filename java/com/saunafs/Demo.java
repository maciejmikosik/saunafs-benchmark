package com.saunafs;

import static com.saunafs.common.io.InetServer.server;
import static com.saunafs.common.io.IoFactories.address;
import static com.saunafs.common.io.IoFactories.socketAddress;
import static com.saunafs.proto.data.Size.mebibytes;
import static com.saunafs.proto.msg.MessageBuilder.message;
import static com.saunafs.proto.msn.LoggingMessenger.logging;
import static com.saunafs.proto.msn.StreamingMessenger.streamingMessenger;
import static com.saunafs.proto.msn.sniff.Sniffer.sniffer;
import static java.time.Clock.systemUTC;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.saunafs.proto.Messenger;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadErasuredChunk;
import com.saunafs.proto.msn.sniff.Event;
import com.saunafs.proto.msn.sniff.Received;
import com.saunafs.proto.msn.sniff.Sent;

/**
 * saunfs protocol constants
 * https://github.com/leil-io/saunafs/blob/dev/src/protocol/SFSCommunication.h
 *
 * other benchmark constants
 * https://github.com/leil-io/saunafs-sandbox/blob/dev/chunk-benchmark/src/protocol_consts.h
 */
public class Demo {
  public static final InetSocketAddress aNeutrinoLocalWifi = socketAddress(
      address("192.168.168.160"), 9422);
  public static final InetSocketAddress aNeutrinoLocalLan = socketAddress(
      address("192.168.168.96"), 9422);
  public static final InetSocketAddress aNeutrinoGlobal = socketAddress(
      address("cajar.ddnnss.eu"), 9422);

  public static void main(String... args) {
    var server = server(aNeutrinoGlobal);
    var events = new ArrayList<Event>();
    var sniffer = sniffer(events::add, systemUTC());
    var messenger = logging(sniffer.sniff(streamingMessenger(server)));
    try {
      server.connect();
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
