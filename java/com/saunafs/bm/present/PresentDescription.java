package com.saunafs.bm.present;

import static com.saunafs.bm.model.Helpers.filterSuccessful;
import static com.saunafs.bm.present.Formatters.decimalFormatter;
import static com.saunafs.bm.present.Formatters.durationInSeconds;
import static com.saunafs.bm.present.Formatters.mebibytesPerSecond;
import static com.saunafs.bm.present.Formatters.sizeInBytes;
import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Style.style;
import static com.saunafs.common.html.Text.text;
import static com.saunafs.common.quant.Size.bytes;
import static com.saunafs.common.quant.Transfer.transfer;
import static java.time.Duration.ZERO;
import static java.util.Arrays.stream;

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
import com.saunafs.common.quant.Size;
import com.saunafs.common.quant.Transfer;

public class PresentDescription {
  private static final Formatter<Size> sizeFormatter = sizeInBytes();
  private static final Formatter<Duration> durationFormatter = durationInSeconds();
  private static final Formatter<Transfer> transferFormatter = mebibytesPerSecond();
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
    return element("div")
        .add(style()
            .add("display", "grid")
            .add("grid-template-columns", "repeat(4, auto)")
            .add("border", "0.05em solid black")
            .add("width", "fit-content")
            .add("gap", "0em 0em")
            .add("text-align", "right"))
        .nest(rowWithHeaders("chunkId", "time", "size", "speed"))
        .nest(rowWithHeaders(
            chunkIdFormatter.unit(),
            durationFormatter.unit(),
            sizeFormatter.unit(),
            transferFormatter.unit()))
        .nest(rowWithTotals(filterSuccessful(chunks)))
        .nest(chunks, PresentDescription::present);
  }

  private static Element rowWithHeaders(String... headers) {
    var row = element("div")
        .add(style()
            .add("display", "contents")
            .add("text-align", "center"));
    stream(headers).forEach(header -> row
        .nest(cell(header)));
    return row;
  }

  private static Nestable rowWithTotals(List<Chunk> chunks) {
    var rowStyle = style()
        .add("display", "contents")
        .add("font-weight", "bold");
    if (chunks.size() > 0) {
      var totalDuration = chunks.stream()
          .map(chunk -> chunk.result.time.duration())
          .reduce(ZERO, Duration::plus);
      var totalBytes = chunks.stream()
          .map(chunk -> chunk.size)
          .reduce(Size::plus)
          .orElse(bytes(0));
      var totalTransfer = transfer(totalBytes, totalDuration);
      return element("div")
          .add(rowStyle)
          .nest(cell("total"))
          .nest(cell(durationFormatter.format(totalDuration)))
          .nest(cell(sizeFormatter.format(totalBytes)))
          .nest(cell(transferFormatter.format(totalTransfer)));
    } else {
      return element("div")
          .add(rowStyle)
          .nest(cell("total"))
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

  private static Element present(Chunk chunk) {
    return chunk.result.status == 0
        ? presentSuccessful(chunk)
        : presentFailed(chunk);
  }

  private static Element presentSuccessful(Chunk chunk) {
    return element("div")
        .add(style()
            .add("display", "contents"))
        .nest(cell(chunkIdFormatter.format(chunk.id)))
        .nest(cell(durationFormatter.format(chunk.result.time.duration())))
        .nest(cell(sizeFormatter.format(chunk.size)))
        .nest(cell(transferFormatter.format(transfer(
            chunk.size,
            chunk.result.time.duration()))));
  }

  private static Element presentFailed(Chunk chunk) {
    return element("div")
        .add(style()
            .add("display", "contents")
            .add("color", "red"))
        .nest(cell(Long.toString(chunk.id)))
        .nest(cell(durationFormatter.format(chunk.result.time.duration())))
        .nest(cell(sizeFormatter.format(chunk.size)))
        .nest(cell("N/A"));
  }
}
