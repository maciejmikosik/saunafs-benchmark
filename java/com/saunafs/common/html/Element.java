package com.saunafs.common.html;

import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Element implements Nestable {
  public String name;
  public List<Attribute> attributes = new LinkedList<>();
  public List<Nestable> nested = new LinkedList<>();

  private Element(String name) {
    this.name = name;
  }

  public static Element element(String name) {
    return new Element(requireNonNull(name));
  }

  public Element add(Attribute attribute) {
    attributes.add(attribute);
    return this;
  }

  public Element nest(Nestable nestable) {
    nested.add(nestable);
    return this;
  }

  public Element nest(List<? extends Nestable> nestables) {
    this.nested.addAll(nestables);
    return this;
  }

  public <T> Element nest(
      List<T> items,
      Function<? super T, ? extends Element> mapping) {
    return nest(items.stream()
        .map(mapping)
        .toList());
  }
}
