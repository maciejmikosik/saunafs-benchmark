package com.saunafs.common;

import java.io.FilterInputStream;
import java.io.InputStream;

public class DelegatingInputStream extends FilterInputStream {
  public DelegatingInputStream() {
    super(null);
  }

  public void delegate(InputStream input) {
    super.in = input;
  }
}
