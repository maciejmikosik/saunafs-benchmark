package com.saunafs.bm.present;

import com.saunafs.common.html.Element;

public interface Presenter<M> {
  Element present(M model);
}
