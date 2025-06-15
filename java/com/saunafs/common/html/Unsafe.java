package com.saunafs.common.html;

import static java.util.Objects.requireNonNull;

public class Unsafe implements Nestable {
  public final String html;

  private Unsafe(String html) {
    this.html = html;
  }

  public static Unsafe unsafe(String html) {
    return new Unsafe(requireNonNull(html));
  }
}
