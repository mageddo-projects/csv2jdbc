package com.mageddo.csv2jdbc;

public class Validator {

  private Validator() {
  }

  public static void isTrue(boolean expression, String msg, Object... args) {
    if (!expression) {
      throw new IllegalArgumentException(String.format(msg, args));
    }
  }
}
