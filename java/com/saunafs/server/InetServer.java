package com.saunafs.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.saunafs.common.DelegatingInputStream;
import com.saunafs.common.DelegatingOutputStream;

public class InetServer implements Server {
  private final InetSocketAddress socketAddress;
  private final Socket socket = new Socket();
  private final DelegatingOutputStream output = new DelegatingOutputStream();
  private final DelegatingInputStream input = new DelegatingInputStream();

  private InetServer(InetSocketAddress socketAddress) {
    this.socketAddress = socketAddress;
  }

  public static Server server(InetSocketAddress socketAddress) {
    return new InetServer(socketAddress);
  }

  public void connect() {
    try {
      socket.connect(socketAddress);
      socket.setTcpNoDelay(true);
      output.delegate(socket.getOutputStream());
      input.delegate(socket.getInputStream());
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
    return output;
  }

  public InputStream input() {
    return input;
  }
}
