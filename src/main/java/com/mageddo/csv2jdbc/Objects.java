package com.mageddo.csv2jdbc;

public class Objects {
  public static <T> T firstNonNull(T... o) {
    for (T t : o) {
      if (t != null) {
        return t;
      }
    }
    return null;
  }
}
