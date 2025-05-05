package com.saunafs.proto;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Identifier {
  int code();

  int version();
}
