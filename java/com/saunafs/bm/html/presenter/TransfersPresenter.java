package com.saunafs.bm.html.presenter;

import static com.saunafs.bm.html.model.FractionWithThreshold.fractionWithThreshold;
import static com.saunafs.bm.html.presenter.BarPresenter.barPresenter;
import static com.saunafs.bm.html.presenter.Formatters.durationInSeconds;
import static com.saunafs.bm.html.presenter.Formatters.mebibytesPerSecond;
import static com.saunafs.bm.html.presenter.Formatters.sizeInBytes;
import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Em.em;
import static com.saunafs.common.html.Style.style;
import static com.saunafs.common.html.Text.text;
import static com.saunafs.common.quant.Rate.rate;
import static com.saunafs.common.quant.Size.bytes;
import static java.util.Comparator.naturalOrder;

import java.time.Duration;
import java.util.List;

import com.saunafs.bm.html.model.Transfer;
import com.saunafs.common.html.Element;
import com.saunafs.common.html.Nestable;
import com.saunafs.common.quant.Rate;
import com.saunafs.common.quant.Size;

public class TransfersPresenter implements Presenter<List<Transfer>> {
  private String itemName;
  private String itemUnit;

  public TransfersPresenter item(String itemName, String itemUnit) {
    this.itemName = itemName;
    this.itemUnit = itemUnit;
    return this;
  }

  private TransfersPresenter() {}

  public static TransfersPresenter transfersPresenter() {
    return new TransfersPresenter();
  }

  public Element present(List<Transfer> model) {
    calculateStatistics(model.stream()
        .filter(transfer -> transfer.status.isOk())
        .toList());
    return presentModel(model);
  }

  private boolean hasSuccessfulTransfer;
  private Duration totalDuration;
  private Size totalBytes;
  private Rate averageRate;
  private Rate maxRate;

  private void calculateStatistics(List<Transfer> model) {
    hasSuccessfulTransfer = !model.isEmpty();
    if (hasSuccessfulTransfer) {
      totalDuration = model.stream()
          .map(transfer -> transfer.duration)
          .reduce(Duration::plus)
          .orElseThrow();
      totalBytes = model.stream()
          .map(transfer -> transfer.size)
          .reduce(Size::plus)
          .orElse(bytes(0));
      averageRate = rate(totalBytes, totalDuration);
      maxRate = model.stream()
          .map(transfer -> rate(transfer.size, transfer.duration))
          .max(naturalOrder())
          .orElseThrow();
    }
  }

  private Element presentModel(List<Transfer> model) {
    return element("div")
        .add(style()
            .add("display", "grid")
            .add("grid-template-columns", "repeat(5, auto)")
            .add("border", "0.05em solid black")
            .add("width", "fit-content")
            .add("gap", "0em 0em")
            .add("text-align", "right")
            .add("white-space", "nowrap"))
        .nest(presentHeaders(
            itemName,
            "size",
            "duration",
            "rate",
            ""))
        .nest(presentHeaders(
            itemUnit,
            sizeFormatter.unit(),
            durationFormatter.unit(),
            rateFormatter.unit(),
            ""))
        .nest(hasSuccessfulTransfer
            ? element("div")
                .add(style()
                    .add("display", "contents")
                    .add("font-weight", "bold"))
                .nest(cell("total"))
                .nest(cell(sizeFormatter.format(totalBytes)))
                .nest(cell(durationFormatter.format(totalDuration)))
                .nest(cell(rateFormatter.format(averageRate)))
                .nest(element("div")
                    .add(style()
                        .add("border", "0.05em solid black"))
                    .nest(barPresenter
                        .present(fractionWithThreshold(
                            averageRate.divideBy(maxRate),
                            averageRate.divideBy(maxRate)))))
            : none())
        .nest(model, this::present);
  }

  private Element presentHeaders(String... headers) {
    return element("div")
        .add(style()
            .add("display", "contents")
            .add("text-align", "center"))
        .nest(headers, this::cell);
  }

  private Element present(Transfer transfer) {
    return transfer.status.isOk()
        ? presentSuccessful(transfer)
        : presentFailed(transfer);
  }

  private Element presentSuccessful(Transfer transfer) {
    var rate = rate(transfer.size, transfer.duration);
    return element("div")
        .add(style()
            .add("display", "contents"))
        .nest(cell(transfer.item.toString()))
        .nest(cell(sizeFormatter.format(transfer.size)))
        .nest(cell(durationFormatter.format(transfer.duration)))
        .nest(cell(rateFormatter.format(rate)))
        .nest(element("div")
            .add(style()
                .add("border", "0.05em solid black"))
            .nest(barPresenter
                .present(fractionWithThreshold(
                    rate.divideBy(maxRate),
                    averageRate.divideBy(maxRate)))));
  }

  private Element presentFailed(Transfer transfer) {
    return element("div")
        .add(style()
            .add("display", "contents")
            .add("color", "red"))
        .nest(cell(transfer.item.toString()))
        .nest(cell(sizeFormatter.format(transfer.size)))
        .nest(cell(""))
        .nest(cell(""))
        .nest(cell(""));
  }

  private Element cell(String string) {
    return cell(text(string));
  }

  private Element cell(Nestable nestable) {
    return element("div")
        .add(style()
            .add("border", "0.05em solid black")
            .add("padding", "0.2em 1em"))
        .nest(nestable);
  }

  private Element none() {
    return element("div")
        .add(style()
            .add("display", "none"));
  }

  private static final BarPresenter barPresenter = barPresenter()
      .length(em(10))
      .thickness(em(1.5))
      .fractionColor("LightBlue")
      .thresholdColor("red");

  private static final Formatter<Size> sizeFormatter = sizeInBytes();
  private static final Formatter<Duration> durationFormatter = durationInSeconds();
  private static final Formatter<Rate> rateFormatter = mebibytesPerSecond();
}
