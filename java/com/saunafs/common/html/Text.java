package com.saunafs.common.html;

import static java.util.Objects.requireNonNull;

public class Text implements Nestable {
  public final String string;

  private Text(String string) {
    this.string = string;
  }

  public static Text text(String string) {
    return new Text(requireNonNull(string));
  }
}
