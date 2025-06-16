package com.saunafs.bm.present;

import static com.saunafs.bm.model.Helpers.filterSuccessful;
import static com.saunafs.bm.model.Helpers.isSuccessful;
import static com.saunafs.bm.present.BarPresenter.barPresenter;
import static com.saunafs.bm.present.Em.em;
import static com.saunafs.bm.present.Formatters.decimalFormatter;
import static com.saunafs.bm.present.Formatters.durationInSeconds;
import static com.saunafs.bm.present.Formatters.mebibytesPerSecond;
import static com.saunafs.bm.present.Formatters.sizeInBytes;
import static com.saunafs.bm.present.FractionWithThreshold.fractionWithThreshold;
import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Style.style;
import static com.saunafs.common.html.Text.text;
import static com.saunafs.common.quant.Rate.rate;
import static com.saunafs.common.quant.Size.bytes;
import static java.util.Comparator.naturalOrder;

import java.time.Duration;
import java.util.List;

import com.saunafs.bm.model.Chunk;
import com.saunafs.common.html.Element;
import com.saunafs.common.html.Nestable;
import com.saunafs.common.quant.Formatter;
import com.saunafs.common.quant.Rate;
import com.saunafs.common.quant.Size;

public class TransfersPresenter implements Presenter<List<Chunk>> {
  private boolean hasSuccessfulTransfer;
  private Duration totalDuration;
  private Size totalBytes;
  private Rate averageRate;
  private Rate maxRate;

  public Element present(List<Chunk> model) {
    calculateStatistics(filterSuccessful(model));
    return presentModel(model);
  }

  private void calculateStatistics(List<Chunk> model) {
    hasSuccessfulTransfer = !model.isEmpty();
    if (hasSuccessfulTransfer) {
      totalDuration = model.stream()
          .map(chunk -> chunk.result.time.duration())
          .reduce(Duration::plus)
          .orElseThrow();
      totalBytes = model.stream()
          .map(chunk -> chunk.size)
          .reduce(Size::plus)
          .orElse(bytes(0));
      averageRate = rate(totalBytes, totalDuration);
      maxRate = model.stream()
          .map(chunk -> rate(chunk.size, chunk.result.time.duration()))
          .max(naturalOrder())
          .orElseThrow();
    }
  }

  private Element presentModel(List<Chunk> model) {
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
            "chunkId",
            "size",
            "duration",
            "rate",
            ""))
        .nest(presentHeaders(
            chunkIdFormatter.unit(),
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

  private Element present(Chunk chunk) {
    return isSuccessful(chunk)
        ? presentSuccessful(chunk)
        : presentFailed(chunk);
  }

  private Element presentSuccessful(Chunk chunk) {
    var rate = rate(chunk.size, chunk.result.time.duration());
    return element("div")
        .add(style()
            .add("display", "contents"))
        .nest(cell(chunkIdFormatter.format(chunk.id)))
        .nest(cell(sizeFormatter.format(chunk.size)))
        .nest(cell(durationFormatter.format(chunk.result.time.duration())))
        .nest(cell(rateFormatter.format(rate)))
        .nest(element("div")
            .add(style()
                .add("border", "0.05em solid black"))
            .nest(barPresenter
                .present(fractionWithThreshold(
                    rate.divideBy(maxRate),
                    averageRate.divideBy(maxRate)))));
  }

  private Element presentFailed(Chunk chunk) {
    return element("div")
        .add(style()
            .add("display", "contents")
            .add("color", "red"))
        .nest(cell(chunkIdFormatter.format(chunk.id)))
        .nest(cell(sizeFormatter.format(chunk.size)))
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

  private static final Formatter<Long> chunkIdFormatter = decimalFormatter();
  private static final Formatter<Size> sizeFormatter = sizeInBytes();
  private static final Formatter<Duration> durationFormatter = durationInSeconds();
  private static final Formatter<Rate> rateFormatter = mebibytesPerSecond();
}
