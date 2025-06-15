package com.saunafs.common.html;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/*
 * TODO Defining same property twice makes last one active due to html rules. It would be nice if
 * {@link Style} didn't rely on this behavior and instead replace previous property itself.
 */
public class Style extends Attribute {
  protected Style(String name, String value) {
    super(name, value);
  }

  public static Style style() {
    return new Style("style", "");
  }

  public static Style style(String cssProperty, String cssValue) {
    return style().add(cssProperty, cssValue);
  }

  public Style add(String cssProperty, String cssValue) {
    requireNonNull(cssProperty);
    requireNonNull(cssValue);
    return new Style(name, format("%s%s:%s;", value, cssProperty, cssValue));
  }
}
