package com.saunafs.bm.html.presenter;

import static com.saunafs.bm.html.presenter.TransfersPresenter.transfersPresenter;
import static com.saunafs.common.html.Element.element;
import static com.saunafs.common.html.Style.style;
import static com.saunafs.common.html.Text.text;

import com.saunafs.bm.html.model.Transfer;
import com.saunafs.bm.model.Chunk;
import com.saunafs.bm.model.ChunkServer;
import com.saunafs.bm.model.Description;
import com.saunafs.bm.model.Disk;
import com.saunafs.common.html.Element;
import com.saunafs.common.html.Style;

public class PresentDescription {
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
        .nest(transfersPresenter()
            .item("chunkId", "DEC")
            .present(disk.chunks.stream()
                .map(PresentDescription::transfer)
                .toList()));
  }

  private static Transfer transfer(Chunk chunk) {
    var transfer = new Transfer();
    transfer.item = Long.toString(chunk.id);
    transfer.status = chunk.result.status;
    transfer.size = chunk.size;
    transfer.duration = chunk.result.time.duration();
    return transfer;
  }
}
