package com.saunafs.bm;

import static com.saunafs.common.html.Attribute.attribute;
import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Text.text;

import java.time.Duration;

import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Description;
import com.saunafs.bm.model.Disk;
import com.saunafs.bm.model.Json;
import com.saunafs.common.html.Attribute;
import com.saunafs.common.html.Element;
import com.saunafs.common.html.Serializer;
import com.saunafs.proto.data.Size;

public class Present {
  public static void main(String... args) {
    var json = new Json();
    var description = json.parse(System.in);
    var html = present(description);
    System.out.println(new Serializer().serialize(html));
  }

  private static final Attribute borderStyle = attribute("style", "border: 1px solid black");

  private static Element present(Description description) {
    return element("div")
        .add(borderStyle)
        .nest(element("div")
            .nest(text("benchmark: " + description.benchmark)))
        .nest(description.cluster, Present::present);
  }

  private static Element present(ChunkServer chunkServer) {
    return element("div")
        .add(borderStyle)
        .nest(element("div")
            .nest(text(chunkServer.address.toString())))
        .nest(chunkServer.disks, Present::present);
  }

  private static Element present(Disk disk) {
    return element("div")
        .add(borderStyle)
        .nest(element("div")
            .nest(text(disk.location)))
        .nest(disk.chunks, Present::present);
  }

  private static Element present(Chunk chunk) {
    return element("div")
        .nest(element("span")
            .add(borderStyle)
            .nest(text(Long.toString(chunk.id))))
        .nest(element("span")
            .add(borderStyle)
            .nest(text(format(chunk.result.time.duration()))))
        .nest(element("span")
            .add(borderStyle)
            .nest(text(chunk.size.inBytes() + " B")))
        .nest(element("span")
            .add(borderStyle)
            .nest(text(formatTransfer(transfer(chunk.size, chunk.result.time.duration())))));
  }

  private static double transfer(Size size, Duration duration) {
    return (size.inBytes()) / (duration.toNanos() * 1e-9);
  }

  private static String format(Duration duration) {
    return "%.9f s".formatted(duration.toNanos() * 1e-9);
  }

  private static String formatTransfer(double transfer) {
    return "%.3f MiB/s".formatted(transfer / (1 << 20));
  }
}
