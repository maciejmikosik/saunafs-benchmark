package com.saunafs.bm.present;

import static com.saunafs.bm.model.Helpers.filterSuccessful;
import static com.saunafs.bm.present.Formatters.decimalFormatter;
import static com.saunafs.bm.present.Formatters.durationInSeconds;
import static com.saunafs.bm.present.Formatters.mebibytesPerSecond;
import static com.saunafs.bm.present.Formatters.sizeInBytes;
import static com.saunafs.common.html.Attribute.attribute;
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
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Description;
import com.saunafs.bm.model.Disk;
import com.saunafs.common.html.Element;
import com.saunafs.common.html.Nestable;
import com.saunafs.common.html.Style;
import com.saunafs.common.quant.Formatter;
import com.saunafs.common.quant.Rate;
import com.saunafs.common.quant.Size;

public class PresentDescription {
  private static final Formatter<Size> sizeFormatter = sizeInBytes();
  private static final Formatter<Duration> durationFormatter = durationInSeconds();
  private static final Formatter<Rate> rateFormatter = mebibytesPerSecond();
  private static final Formatter<Long> chunkIdFormatter = decimalFormatter();

  private static final Style panelStyle = style()
      .add("border", "1px solid black")
      .add("padding", "0.5em 0.5em");

  public static Element present(Description description) {
    return element("div")
        .add(panelStyle)
        .nest(element("div")
            .nest(text("benchmark: " + description.benchmark)))
        .nest(description.cluster, PresentDescription::present);
  }

  private static Element present(ChunkServer chunkServer) {
    return element("div")
        .add(panelStyle)
        .nest(element("div")
            .nest(text(chunkServer.address.toString())))
        .nest(chunkServer.disks, PresentDescription::present);
  }

  private static Element present(Disk disk) {
    return element("div")
        .add(panelStyle)
        .nest(text(disk.location))
        .nest(tableOf(disk.chunks));
  }

  private static Element tableOf(List<Chunk> chunks) {
    var totalDuration = chunks.stream()
        .map(chunk -> chunk.result.time.duration())
        .reduce(Duration::plus)
        .orElse(ZERO);
    var totalBytes = chunks.stream()
        .map(chunk -> chunk.size)
        .reduce(Size::plus)
        .orElse(bytes(0));
    var averageRate = rate(totalBytes, totalDuration);

    var maxRate = chunks.stream()
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
        .nest(rowWithTotals(filterSuccessful(chunks)))
        .nest(chunks, chunk -> present(chunk, averageRate, maxRate));
  }

  private static Element rowWithHeaders(String... headers) {
    return element("div")
        .add(style()
            .add("display", "contents")
            .add("text-align", "center"))
        .nest(headers, PresentDescription::cell);
  }

  private static Nestable rowWithTotals(List<Chunk> chunks) {
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

  private static Element cell(String string) {
    return element("div")
        .add(style()
            .add("border", "0.05em solid black")
            .add("white-space", "nowrap")
            .add("padding", "0.2em 1em"))
        .nest(text(string));
  }

  private static Element present(
      Chunk chunk,
      Rate averageRate,
      Rate maxRate) {
    return chunk.result.status == 0
        ? presentSuccessful(chunk, averageRate, maxRate)
        : presentFailed(chunk);
  }

  private static Element presentSuccessful(
      Chunk chunk,
      Rate averageRate,
      Rate maxRate) {
    var rate = rate(
        chunk.size,
        chunk.result.time.duration());
    var progress = rate.divideBy(maxRate);
    var threshold = averageRate.divideBy(maxRate);
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
            .nest(present(progress, threshold)));
  }

  private static Element present(double progress, double threshold) {
    int width = 10;
    return element("span")
        .nest(element("svg")
            .add(attribute("width", width + "em"))
            .add(attribute("height", "1.5em"))
            .nest(element("rect")
                .add(attribute("width", normalize(width, progress) + "em"))
                .add(attribute("height", "1.5em"))
                .add(attribute("x", "0"))
                .add(attribute("y", "0"))
                .add(attribute("fill", "LightBlue")))
            .nest(element("rect")
                .add(attribute("width", normalize(width, 0.01) + "em"))
                .add(attribute("height", "1.5em"))
                .add(attribute("x", normalize(width, threshold) + "em"))
                .add(attribute("y", "0em"))
                .add(attribute("fill", "red"))
                .add(attribute("stroke-width", "0.1em"))));
  }

  private static String normalize(int max, double progress) {
    return "%.2f".formatted(progress * max);
  }

  private static Element presentFailed(Chunk chunk) {
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
}
