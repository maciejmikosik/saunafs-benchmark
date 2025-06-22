package com.saunafs.common.html;

import static com.saunafs.common.Collections.addLast;
import static com.saunafs.common.html.Attribute.attribute;
import static com.saunafs.common.html.Property.property;
import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.joining;

import java.util.List;

public class Style {
  public List<Property> properties;

  protected Style(List<Property> properties) {
    this.properties = properties;
  }

  public static Style style() {
    return new Style(EMPTY_LIST);
  }

  public Style add(Property property) {
    return new Style(addLast(property, properties));
  }

  public Style add(String cssProperty, String cssValue) {
    return add(property(cssProperty, cssValue));
  }

  public Style add(String cssProperty, Object cssValue) {
    return add(property(cssProperty, cssValue.toString()));
  }

  public Attribute toAttribute() {
    return attribute("style", properties.stream()
        .map(property -> "%s:%s;".formatted(property.name, property.value))
        .collect(joining()));
  }
}
