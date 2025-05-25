package com.saunafs.bm;

import static java.util.stream.Collectors.joining;

import java.time.Duration;

import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Description;
import com.saunafs.bm.model.Disk;
import com.saunafs.bm.model.Json;
import com.saunafs.proto.data.Size;

public class Present {
  public static void main(String... args) {
    var json = new Json();
    var description = json.parse(System.in);
    var html = present(description);
    System.out.println(html);
  }

  private static String present(Description description) {
    return new StringBuilder()
        .append("<div style=\"border: 1px solid black\">")
        .append("<div>").append("benchmark: " + description.benchmark).append("</div>")
        .append(description.cluster.stream()
            .map(Present::present)
            .collect(joining()))
        .append("</div>")
        .toString();
  }

  private static String present(ChunkServer chunkServer) {
    return new StringBuilder()
        .append("<div style=\"border: 1px solid black\">")
        .append("<div>").append(escape(chunkServer.address.toString())).append("</div>")
        .append(chunkServer.disks.stream()
            .map(Present::present)
            .collect(joining()))
        .append("</div>")
        .toString();
  }

  private static String present(Disk disk) {
    return new StringBuilder()
        .append("<div style=\"border: 1px solid black\">")
        .append("<div>").append(escape(disk.location)).append("</div>")
        .append(disk.chunks.stream()
            .map(Present::present)
            .collect(joining()))
        .append("</div>")
        .toString();
  }

  private static String present(Chunk chunk) {
    return new StringBuilder()
        .append("<div>")
        .append("<span style=\"border: 1px solid black\">")
        .append(chunk.id)
        .append("</span>")
        .append("<span style=\"border: 1px solid black\">")
        .append(format(chunk.result.time.duration()))
        .append("</span>")
        .append("<span style=\"border: 1px solid black\">")
        .append(chunk.size.inBytes()).append(" B")
        .append("</span>")
        .append("<span style=\"border: 1px solid black\">")
        .append(formatTransfer(transfer(chunk.size, chunk.result.time.duration())))
        .append("</span>")
        .append("</div>")
        .toString();
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

  private static String escape(String raw) {
    return raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("\'", "&#39;");
  }
}
