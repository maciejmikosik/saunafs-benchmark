package com.saunafs.bm.html.presenter;

import static com.saunafs.bm.html.presenter.TransfersPresenter.transfersPresenter;
import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Style.style;
import static com.saunafs.common.html.Text.text;
import static com.saunafs.common.quant.Size.bytes;
import static com.saunafs.proto.data.Status.status;

import java.time.Duration;
import java.util.List;

import com.saunafs.bm.html.model.Transfer;
import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Description;
import com.saunafs.bm.model.Disk;
import com.saunafs.common.html.Element;
import com.saunafs.common.html.Style;
import com.saunafs.common.quant.Size;

public class PresentDescription {
  private static final Style panelStyle = style()
      .add("border", "1px solid black")
      .add("padding", "0.5em 0.5em");

  public static Element present(Description description) {
    return element("div")
        .add(panelStyle)
        .nest(element("div")
            .nest(text("benchmark: " + description.benchmark)))
        .nest(transfersPresenter()
            .item("server")
            .present(description.cluster.stream()
                .map(PresentDescription::transfer)
                .toList()))
        .nest(description.cluster, PresentDescription::present);
  }

  private static Element present(ChunkServer chunkServer) {
    return element("div")
        .add(panelStyle)
        .nest(element("div")
            .nest(text(chunkServer.address.toString())))
        .nest(transfersPresenter()
            .item("disk")
            .present(chunkServer.disks.stream()
                .map(PresentDescription::transfer)
                .toList()))
        .nest(chunkServer.disks, PresentDescription::present);
  }

  private static Element present(Disk disk) {
    return element("div")
        .add(panelStyle)
        .nest(text(disk.location))
        .nest(transfersPresenter()
            .item("chunkId [DEC | HEX]")
            .present(disk.chunks.stream()
                .map(PresentDescription::transfer)
                .toList()));
  }

  private static Transfer transfer(ChunkServer server) {
    var transfer = merge(server.disks.stream()
        .flatMap(disk -> disk.chunks.stream())
        .map(PresentDescription::transfer)
        .toList());
    transfer.item = server.address.toString();
    return transfer;
  }

  private static Transfer transfer(Disk disk) {
    var transfer = merge(disk.chunks.stream()
        .map(PresentDescription::transfer)
        .toList());
    transfer.item = disk.location;
    return transfer;
  }

  private static Transfer transfer(Chunk chunk) {
    var transfer = new Transfer();
    transfer.item = "%d | %016X".formatted(chunk.id, chunk.id);
    transfer.status = chunk.result.status;
    transfer.size = chunk.size;
    transfer.duration = chunk.result.time.duration();
    return transfer;
  }

  private static Transfer merge(List<Transfer> transfers) {
    var merged = new Transfer();
    merged.status = transfers.stream()
        .map(transfer -> transfer.status)
        .filter(status -> status.isOk())
        .findFirst()
        .orElse(status((byte) -1));
    merged.size = transfers.stream()
        .filter(transfer -> transfer.status.isOk())
        .map(transfer -> transfer.size)
        .reduce(Size::plus)
        .orElse(bytes(0));
    merged.duration = transfers.stream()
        .filter(transfer -> transfer.status.isOk())
        .map(transfer -> transfer.duration)
        .reduce(Duration::plus)
        .orElse(Duration.ZERO);
    return merged;
  }
}
