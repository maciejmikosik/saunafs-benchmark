package com.saunafs.server;

import static com.saunafs.common.Common.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class InetServer implements Server {
  private final InetSocketAddress socketAddress;
  private Socket socket;

  private InetServer(InetSocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }

  public static Server server(InetSocketAddress socketAddress) {
    return new InetServer(socketAddress);
  }

  public void connect() {
    try {
      socket = socket(socketAddress);
      socket.setTcpNoDelay(true);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void disconnect() {
    try {
      socket.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public OutputStream output() {
    try {
      return socket.getOutputStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public InputStream input() {
    try {
      return socket.getInputStream();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
