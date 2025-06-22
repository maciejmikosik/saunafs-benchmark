package com.saunafs.bm.html.presenter;

import com.saunafs.common.html.Element;

public interface Presenter<M> {
  Element present(M model);
}
