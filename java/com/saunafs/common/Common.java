package com.saunafs.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Common {
  public static UncheckedIOException unchecked(IOException exception) {
    return new UncheckedIOException(exception);
  }

  public static BufferedInputStream buffered(InputStream input) {
    return new BufferedInputStream(input);
  }

  public static BufferedOutputStream buffered(OutputStream output) {
    return new BufferedOutputStream(output);
  }

  public static DataInputStream data(InputStream input) {
    return new DataInputStream(input);
  }

  public static DataOutputStream data(OutputStream output) {
    return new DataOutputStream(output);
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
