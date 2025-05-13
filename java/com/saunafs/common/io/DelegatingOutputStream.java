package com.saunafs.common.io;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public class DelegatingOutputStream extends FilterOutputStream {
  public DelegatingOutputStream() {
    super(null);
  }

  public void delegate(OutputStream output) {
    super.out = output;
  }
}
