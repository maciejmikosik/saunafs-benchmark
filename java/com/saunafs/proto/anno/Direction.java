package com.saunafs.proto.anno;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface Direction {
  Machine from();

  Machine to();
}
