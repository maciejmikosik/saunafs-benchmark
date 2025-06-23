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
import static com.saunafs.common.html.Widgets.center;
import static com.saunafs.common.html.Widgets.contents;
import static com.saunafs.common.html.Widgets.rightCenter;
import static com.saunafs.common.html.Widgets.stacked;
import static com.saunafs.common.quant.Rate.rate;
import static com.saunafs.common.quant.Size.bytes;
import static com.saunafs.proto.data.Status.status;
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

  public TransfersPresenter item(String itemName) {
    this.itemName = itemName;
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
  private final Transfer total = new Transfer();
  private Rate maxRate;
  private double threshold;

  private void calculateStatistics(List<Transfer> model) {
    hasSuccessfulTransfer = !model.isEmpty();
    if (hasSuccessfulTransfer) {
      total.item = "total";
      total.status = status((byte) 0);
      total.size = model.stream()
          .map(transfer -> transfer.size)
          .reduce(Size::plus)
          .orElse(bytes(0));
      total.duration = model.stream()
          .map(transfer -> transfer.duration)
          .reduce(Duration::plus)
          .orElseThrow();
      maxRate = model.stream()
          .map(transfer -> rate(transfer.size, transfer.duration))
          .max(naturalOrder())
          .orElseThrow();
      var totalRate = rate(total.size, total.duration);
      threshold = totalRate.divideBy(maxRate);
    } else {
      total.status = status((byte) -1);
    }
  }

  private Element presentModel(List<Transfer> model) {
    return element("div")
        .add(style()
            .add("display", "grid")
            .add("grid-template-columns", "repeat(4, auto)")
            .add("width", "fit-content")
            .add("gap", "0em 0em")
            .add("white-space", "nowrap"))
        .nest(contents(
            header(itemName),
            header("size [%s]".formatted(sizeFormatter.unit())),
            header("duration [%s]".formatted(durationFormatter.unit())),
            header("rate [%s]".formatted(rateFormatter.unit()))))
        .nest(hasSuccessfulTransfer
            ? element("span")
                .add(style()
                    .add("display", "contents")
                    .add("font-weight", "bold"))
                .nest(present(total))
            : none())
        .nest(model, this::present);
  }

  private static Element header(String label) {
    return border(center(padding(text(label))));
  }

  private Element present(Transfer transfer) {
    return transfer.status.isOk()
        ? presentSuccessful(transfer)
        : presentFailed(transfer);
  }

  private Element presentSuccessful(Transfer transfer) {
    var rate = rate(transfer.size, transfer.duration);
    return contents(
        cell(transfer.item.toString()),
        cell(sizeFormatter.format(transfer.size)),
        cell(durationFormatter.format(transfer.duration)),
        border(center(stacked(
            barPresenter.present(fractionWithThreshold(
                rate.divideBy(maxRate),
                threshold)),
            text(rateFormatter.format(rate))))));
  }

  private Element presentFailed(Transfer transfer) {
    return red(contents(
        cell(transfer.item.toString()),
        cell(sizeFormatter.format(transfer.size)),
        cell(""),
        cell("")));
  }

  private static Element red(Nestable nestable) {
    return element("span")
        .add(style()
            .add("display", "contents")
            .add("color", "red"))
        .nest(nestable);
  }

  private Element cell(String string) {
    return cell(text(string));
  }

  private Element cell(Nestable nestable) {
    return border(rightCenter(padding(nestable)));
  }

  private Element none() {
    return element("div")
        .add(style()
            .add("display", "none"));
  }

  private static Element padding(Nestable nestable) {
    return element("span")
        .add(style()
            .add("padding", "0.2em 1em"))
        .nest(nestable);
  }

  private static Element border(Nestable nestable) {
    return element("span")
        .add(style()
            .add("border", "0.05em solid black"))
        .nest(nestable);
  }

  private static final BarPresenter barPresenter = barPresenter()
      .length(em(10))
      .thickness(em(1.6))
      .fractionColor("LightBlue")
      .thresholdColor("red");

  private static final Formatter<Size> sizeFormatter = sizeInBytes();
  private static final Formatter<Duration> durationFormatter = durationInSeconds();
  private static final Formatter<Rate> rateFormatter = mebibytesPerSecond();
}
