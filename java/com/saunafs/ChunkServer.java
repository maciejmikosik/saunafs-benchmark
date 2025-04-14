package com.saunafs;

import static com.saunafs.Common.socket;
import static com.saunafs.ReadStatus.readStatus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
      var messageType = input.readInt();
      var length = input.readInt();
      return switch (messageType) {
        case ReadStatus.messageType -> readStatus(input);
        default -> throw new RuntimeException(
            "unknown message type %d and length %d"
                .formatted(messageType, length));
      };
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
