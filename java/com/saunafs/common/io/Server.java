package com.saunafs.common.io;

import java.io.InputStream;
import java.io.OutputStream;

public interface Server {
  void connect();

  void disconnect();

  OutputStream output();

  InputStream input();
}
