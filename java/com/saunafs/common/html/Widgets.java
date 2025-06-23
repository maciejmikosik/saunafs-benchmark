package com.saunafs.common.html;

import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Style.style;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public class Widgets {
  public static Element stacked(Nestable... nestables) {
    return element("span")
        .add(style()
            .add("display", "grid"))
        .nest(stream(nestables)
            .map(Widgets::cell)
            .toList());
  }

  private static Element cell(Nestable nestable) {
    return element("span")
        .add(style()
            .add("grid-row", 1)
            .add("grid-column", 1)
            .add("display", "inline-flex")
            .add("align-items", "center")
            .add("justify-content", "center"))
        .nest(nestable);
  }

  public static Element center(Nestable nestable) {
    return element("div")
        .add(style()
            .add("width", "100%")
            .add("height", "100%")
            .add("display", "inline-flex")
            .add("align-items", "center")
            .add("justify-content", "center"))
        .nest(nestable);
  }

  public static Element rightCenter(Nestable nestable) {
    return element("div")
        .add(style()
            .add("width", "100%")
            .add("height", "100%")
            .add("display", "inline-flex")
            .add("align-items", "center")
            .add("justify-content", "right"))
        .nest(nestable);
  }

  public static Element contents(Nestable... nestables) {
    return element("span")
        .add(style()
            .add("display", "contents"))
        .nest(asList(nestables));
  }
}
