package com.mageddo.csv2jdbc;

public class StringUtils {
  public static String removeFromStartEnd(String source, CharSequence c) {
    return removeFromStartEnd(source, c, c);
  }

  public static String removeFromStartEnd(String source, CharSequence s, CharSequence e) {
    return source
        .replaceAll("^" + s, "")
        .replaceAll(e + "$", "");
  }
}
