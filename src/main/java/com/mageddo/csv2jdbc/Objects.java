package com.mageddo.csv2jdbc;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class Objects {
  public static <T> T firstNonNull(T... o) {
    for (T t : o) {
      if (t != null) {
        return t;
      }
    }
    return null;
  }

  public static <T, R> R mapOrNull(T o, Function<T, R> fn) {
    return Optional
        .ofNullable(o)
        .map(fn)
        .orElse(null);
  }

  public static <T> T useItOrDefault(T o, Supplier<T> s) {
    if (o == null) {
      return s.get();
    }
    return o;
  }
}
