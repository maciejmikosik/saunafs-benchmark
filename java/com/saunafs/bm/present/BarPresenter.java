package com.saunafs.bm.present;

import static com.saunafs.bm.present.Em.em;
import static com.saunafs.common.html.Attribute.attribute;
import static com.saunafs.common.html.Element.element;

import com.saunafs.common.html.Element;

public class BarPresenter implements Presenter<FractionWithThreshold> {
  private Em length = em(10);
  private Em thickness = em(1.5);
  private String fractionColor = "LightBlue";
  private String thresholdColor = "red";

  public static BarPresenter barPresenter() {
    return new BarPresenter();
  }

  public BarPresenter length(Em length) {
    this.length = length;
    return this;
  }

  public BarPresenter thickness(Em thickness) {
    this.thickness = thickness;
    return this;
  }

  public BarPresenter fractionColor(String fractionColor) {
    this.fractionColor = fractionColor;
    return this;
  }

  public BarPresenter thresholdColor(String thresholdColor) {
    this.thresholdColor = thresholdColor;
    return this;
  }

  public Element present(FractionWithThreshold model) {
    return element("svg")
        .add(attribute("width", length))
        .add(attribute("height", thickness))
        .nest(element("rect")
            .add(attribute("width", length.multiply(model.fraction)))
            .add(attribute("height", thickness))
            .add(attribute("x", em(0)))
            .add(attribute("y", em(0)))
            .add(attribute("fill", fractionColor)))
        .nest(element("rect")
            .add(attribute("width", length.multiply(0.01)))
            .add(attribute("height", thickness))
            .add(attribute("x", length.multiply(model.threshold)))
            .add(attribute("y", em(0)))
            .add(attribute("fill", thresholdColor))
            .add(attribute("stroke-width", em(0.1))));
  }
}
