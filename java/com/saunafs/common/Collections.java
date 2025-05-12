package com.saunafs.common;

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
}
