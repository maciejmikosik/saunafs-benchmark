package com.saunafs.bm;

import static com.saunafs.bm.model.Helpers.filterSuccessful;
import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Style.style;
import static com.saunafs.common.html.Text.text;
import static com.saunafs.proto.data.Size.bytes;
import static java.time.Duration.ZERO;
import static java.util.Arrays.stream;

import java.time.Duration;
import java.util.List;

import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Description;
import com.saunafs.bm.model.Disk;
import com.saunafs.bm.model.Json;
import com.saunafs.common.html.Element;
import com.saunafs.common.html.Nestable;
import com.saunafs.common.html.Serializer;
import com.saunafs.common.html.Style;
import com.saunafs.proto.data.Size;

public class Present {
  public static void main(String... args) {
    var json = new Json();
    var description = json.parse(System.in);
    var html = present(description);
    System.out.println(new Serializer().serialize(html));
  }

  private static final Style panelStyle = style()
      .add("border", "1px solid black")
      .add("padding", "0.5em 0.5em");

  private static Element present(Description description) {
    return element("div")
        .add(panelStyle)
        .nest(element("div")
            .nest(text("benchmark: " + description.benchmark)))
        .nest(description.cluster, Present::present);
  }

  private static Element present(ChunkServer chunkServer) {
    return element("div")
        .add(panelStyle)
        .nest(element("div")
            .nest(text(chunkServer.address.toString())))
        .nest(chunkServer.disks, Present::present);
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
        .nest(rowWithHeaders("", "s", "B", "MiB/s"))
        .nest(rowWithTotals(filterSuccessful(chunks)))
        .nest(chunks, Present::present);
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
    if (chunks.size() > 0) {
      var totalDuration = chunks.stream()
          .map(chunk -> chunk.result.time.duration())
          .reduce(ZERO, Duration::plus);
      int totalBytes = chunks.stream()
          .mapToInt(chunk -> chunk.size.inBytes())
          .sum();
      return element("div")
          .add(style()
              .add("display", "contents"))
          .nest(cell("total"))
          .nest(cell(format(totalDuration)))
          .nest(cell("" + totalBytes))
          .nest(cell(formatTransfer(transfer(bytes(totalBytes), totalDuration))));
    } else {
      return element("div")
          .add(style()
              .add("display", "contents"))
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
        .nest(cell(Long.toString(chunk.id)))
        .nest(cell(format(chunk.result.time.duration())))
        .nest(cell("" + chunk.size.inBytes()))
        .nest(cell(formatTransfer(transfer(chunk.size, chunk.result.time.duration()))));
  }

  private static Element presentFailed(Chunk chunk) {
    return element("div")
        .add(style()
            .add("display", "contents")
            .add("color", "red"))
        .nest(cell(Long.toString(chunk.id)))
        .nest(cell(format(chunk.result.time.duration())))
        .nest(cell("" + chunk.size.inBytes()))
        .nest(cell("N/A"));
  }

  private static double transfer(Size size, Duration duration) {
    return (size.inBytes()) / (duration.toNanos() * 1e-9);
  }

  private static String format(Duration duration) {
    return "%.9f".formatted(duration.toNanos() * 1e-9);
  }

  private static String formatTransfer(double transfer) {
    return "%.3f".formatted(transfer / (1 << 20));
  }
}
