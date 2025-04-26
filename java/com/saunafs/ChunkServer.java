package com.saunafs;

import static com.saunafs.common.Common.socket;
import static com.saunafs.proto.msg.ReadData.readData;
import static com.saunafs.proto.msg.ReadStatus.readStatus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.saunafs.proto.Request;
import com.saunafs.proto.Response;
import com.saunafs.proto.msg.ReadData;
import com.saunafs.proto.msg.ReadStatus;

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
      var version = input.readInt();
      return switch (messageType) {
        case ReadStatus.messageType -> switch (version) {
          case 0 -> readStatus(input);
          default -> fail(messageType, length, version);
        };
        case ReadData.messageType -> switch (version) {
          case 0 -> readData(input);
          default -> fail(messageType, length, version);
        };
        default -> fail(messageType, length, version);
      };
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Response fail(int messageType, int length, int version) {
    throw new RuntimeException(
        "unknown message type(%d) length(%d) version(%d)"
            .formatted(messageType, length, version));
  }
}
