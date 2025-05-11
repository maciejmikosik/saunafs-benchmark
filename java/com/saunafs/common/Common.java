package com.saunafs.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Common {
  public static UncheckedIOException unchecked(IOException exception) {
    return new UncheckedIOException(exception);
  }

  public static InetAddress address(String string) {
    try {
      return InetAddress.getByName(string);
    } catch (UnknownHostException e) {
      throw unchecked(e);
    }
  }

  public static InetSocketAddress socketAddress(InetAddress address, int port) {
    return new InetSocketAddress(address, port);
  }
}
