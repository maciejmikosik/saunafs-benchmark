package com.saunafs.common.html;

public class Serializer {
  private StringBuilder builder = new StringBuilder();

  public String serialize(Element element) {
    builder = new StringBuilder();
    append(element);
    return builder.toString();
  }

  private void append(Element root) {
    appendOpeningTag(root);
    root.nested.forEach(nestable -> {
      switch (nestable) {
        case Element element -> append(element);
        case Text text -> append(text);
        default -> throw new RuntimeException();
      }
    });
    appendClosingTag(root);
  }

  private void appendOpeningTag(Element element) {
    builder
        .append("<")
        .append(element.name);
    element.attributes.forEach(this::append);
    builder.append(">");
  }

  private void append(Attribute attribute) {
    builder
        .append(" ")
        .append(attribute.name)
        .append("=\"")
        .append(attribute.value)
        .append("\"");
  }

  private void appendClosingTag(Element element) {
    builder
        .append("</")
        .append(element.name)
        .append(">");
  }

  private void append(Text text) {
    builder.append(escape(text.string));
  }

  private static String escape(String raw) {
    return raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("\'", "&#39;");
  }
}
