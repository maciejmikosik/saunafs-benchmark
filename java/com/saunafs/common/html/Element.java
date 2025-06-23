package com.saunafs.common.html;

import static com.saunafs.common.Collections.addLast;
import static java.util.Arrays.stream;
import static java.util.Collections.EMPTY_LIST;

import java.util.List;
import java.util.function.Function;

public class Element implements Nestable {
  public final String name;
  public final List<Attribute> attributes;
  public final List<Nestable> nested;

  private Element(
      String name,
      List<Attribute> attributes,
      List<Nestable> nested) {
    this.name = name;
    this.attributes = attributes;
    this.nested = nested;
  }

  public static Element element(String name) {
    return new Element(
        name,
        EMPTY_LIST,
        EMPTY_LIST);
  }

  public Element add(Attribute attribute) {
    return new Element(
        name,
        addLast(attribute, attributes),
        nested);
  }

  public Element add(Style style) {
    return add(style.toAttribute());
  }

  public Element nest(Nestable nestable) {
    return new Element(
        name,
        attributes,
        addLast(nestable, nested));
  }

  public Element nest(List<? extends Nestable> nestables) {
    return new Element(
        name,
        attributes,
        addLast(nestables, nested));
  }

  public <T> Element nest(
      List<T> items,
      Function<? super T, ? extends Element> mapping) {
    return nest(items.stream()
        .map(mapping)
        .toList());
  }

  public <T> Element nest(
      T[] items,
      Function<? super T, ? extends Element> mapping) {
    return nest(stream(items)
        .map(mapping)
        .toList());
  }
}
