package com.saunafs.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.saunafs.proto.Message;
import com.saunafs.proto.Protocol;

public class Messenger {
  private final DataOutputStream output;
  private final DataInputStream input;

  Messenger(DataOutputStream output, DataInputStream input) {
    this.output = output;
    this.input = input;
  }

  public static Messenger messenger(Server server) {
    return new Messenger(
        new DataOutputStream(server.output()),
        new DataInputStream(server.input()));
  }

  public void send(Message message) {
    try {
      message.writeTo(output);
      output.flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public Message receive() {
    try {
      var code = input.readInt();
      var length = input.readInt();
      var version = input.readInt();

      // TODO implement lookup table
      return Protocol.PROTOCOL.stream()
          .filter(definition -> code == definition.code && version == definition.version)
          .map(definition -> definition.decoder.apply(input))
          .findFirst()
          .orElseThrow(() -> new RuntimeException(
              "unknown message type(%d) length(%d) version(%d)"
                  .formatted(code, length, version)));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
