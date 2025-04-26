package com.saunafs.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Common {
  public static InetAddress address(String string) {
    try {
      return InetAddress.getByName(string);
    } catch (UnknownHostException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static InetSocketAddress socketAddress(InetAddress address, int port) {
    return new InetSocketAddress(address, port);
  }

  public static Socket socket(InetSocketAddress socketAddress) {
    try {
      return new Socket(socketAddress.getAddress(), socketAddress.getPort());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static Object readStatic(Field field) {
    if (!Modifier.isStatic(field.getModifiers())) {
      throw new IllegalArgumentException();
    }
    try {
      return field.get(null);
    } catch (IllegalAccessException e) {
      throw new RuntimeException();
    }
  }
}
