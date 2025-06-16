package com.saunafs.bm.present;

import static com.saunafs.bm.model.Helpers.filterSuccessful;
import static com.saunafs.bm.present.FractionWithThreshold.fractionWithThreshold;
import static com.saunafs.bm.present.BarPresenter.barPresenter;
import static com.saunafs.bm.present.Em.em;
import static com.saunafs.bm.present.Formatters.decimalFormatter;
import static com.saunafs.bm.present.Formatters.durationInSeconds;
import static com.saunafs.bm.present.Formatters.mebibytesPerSecond;
import static com.saunafs.bm.present.Formatters.sizeInBytes;
import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Style.style;
import static com.saunafs.common.html.Text.text;
import static com.saunafs.common.quant.Rate.rate;
import static com.saunafs.common.quant.Size.bytes;
import static java.time.Duration.ZERO;
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
  public Element present(List<Chunk> model) {
    var totalDuration = model.stream()
        .map(chunk -> chunk.result.time.duration())
        .reduce(Duration::plus)
        .orElse(ZERO);
    var totalBytes = model.stream()
        .map(chunk -> chunk.size)
        .reduce(Size::plus)
        .orElse(bytes(0));
    var averageRate = rate(totalBytes, totalDuration);

    var maxRate = model.stream()
        .map(chunk -> rate(chunk.size, chunk.result.time.duration()))
        .max(naturalOrder())
        .orElse(Rate.ZERO);

    return element("div")
        .add(style()
            .add("display", "grid")
            .add("grid-template-columns", "repeat(5, auto)")
            .add("border", "0.05em solid black")
            .add("width", "fit-content")
            .add("gap", "0em 0em")
            .add("text-align", "right"))
        .nest(rowWithHeaders("chunkId", "time", "size", "rate", ""))
        .nest(rowWithHeaders(
            chunkIdFormatter.unit(),
            durationFormatter.unit(),
            sizeFormatter.unit(),
            rateFormatter.unit(),
            ""))
        .nest(rowWithTotals(filterSuccessful(model)))
        .nest(model, chunk -> present(chunk, averageRate, maxRate));
  }

  private Element rowWithHeaders(String... headers) {
    return element("div")
        .add(style()
            .add("display", "contents")
            .add("text-align", "center"))
        .nest(headers, this::cell);
  }

  private Nestable rowWithTotals(List<Chunk> chunks) {
    var rowStyle = style()
        .add("display", "contents")
        .add("font-weight", "bold");
    if (chunks.size() > 0) {
      var totalDuration = chunks.stream()
          .map(chunk -> chunk.result.time.duration())
          .reduce(Duration::plus)
          .orElse(ZERO);
      var totalBytes = chunks.stream()
          .map(chunk -> chunk.size)
          .reduce(Size::plus)
          .orElse(bytes(0));
      var averageRate = rate(totalBytes, totalDuration);
      return element("div")
          .add(rowStyle)
          .nest(cell("total"))
          .nest(cell(durationFormatter.format(totalDuration)))
          .nest(cell(sizeFormatter.format(totalBytes)))
          .nest(cell(rateFormatter.format(averageRate)))
          .nest(cell(""));
    } else {
      return element("div")
          .add(rowStyle)
          .nest(cell("total"))
          .nest(cell("N/A"))
          .nest(cell("N/A"))
          .nest(cell("N/A"))
          .nest(cell("N/A"));
    }
  }

  private Element present(
      Chunk chunk,
      Rate averageRate,
      Rate maxRate) {
    return chunk.result.status == 0
        ? presentSuccessful(chunk, averageRate, maxRate)
        : presentFailed(chunk);
  }

  private Element presentSuccessful(
      Chunk chunk,
      Rate averageRate,
      Rate maxRate) {
    var rate = rate(
        chunk.size,
        chunk.result.time.duration());
    return element("div")
        .add(style()
            .add("display", "contents"))
        .nest(cell(chunkIdFormatter.format(chunk.id)))
        .nest(cell(durationFormatter.format(chunk.result.time.duration())))
        .nest(cell(sizeFormatter.format(chunk.size)))
        .nest(cell(rateFormatter.format(rate)))
        .nest(element("div")
            .add(style()
                .add("border", "0.05em solid black"))
            .nest(barPresenter()
                .length(em(10))
                .thickness(em(1.5))
                .fractionColor("LightBlue")
                .thresholdColor("red")
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
        .nest(cell(durationFormatter.format(chunk.result.time.duration())))
        .nest(cell(sizeFormatter.format(chunk.size)))
        .nest(cell("N/A"))
        .nest(cell("N/A"));
  }

  private Element cell(String string) {
    return element("div")
        .add(style()
            .add("border", "0.05em solid black")
            .add("white-space", "nowrap")
            .add("padding", "0.2em 1em"))
        .nest(text(string));
  }

  private static final Formatter<Size> sizeFormatter = sizeInBytes();
  private static final Formatter<Duration> durationFormatter = durationInSeconds();
  private static final Formatter<Rate> rateFormatter = mebibytesPerSecond();
  private static final Formatter<Long> chunkIdFormatter = decimalFormatter();
}
