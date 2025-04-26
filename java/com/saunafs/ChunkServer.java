package com.saunafs;

import static com.saunafs.common.Common.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.saunafs.proto.Protocol;
import com.saunafs.proto.Request;
import com.saunafs.proto.Response;

public class ChunkServer {
  private final InetSocketAddress socketAddress;
  private Socket socket;
  public DataInputStream input;
  private DataOutputStream output;

  public ChunkServer(InetSocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }

  public ChunkServer connect() {
    try {
      socket = socket(socketAddress);
      socket.setTcpNoDelay(true);
      input = new DataInputStream(socket.getInputStream());
      output = new DataOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return this;
  }

  public void disconnect() {
    try {
      socket.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void send(Request request) {
    try {
      request.writeTo(output);
      output.flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public Response receive() {
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
