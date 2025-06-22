package com.saunafs.common;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Collections {
  public static <A, B> Collector<Entry<A, B>, ?, Map<A, B>> toMapFromEntries() {
    return Collectors.toMap(
        Entry::getKey,
        Entry::getValue);
  }

  public static <E> List<E> addLast(E element, List<E> list) {
    var newList = new ArrayList<E>(list);
    newList.add(element);
    return unmodifiableList(newList);
  }
}
