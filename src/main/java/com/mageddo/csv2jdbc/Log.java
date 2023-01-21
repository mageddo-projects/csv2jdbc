package com.mageddo.csv2jdbc;

public class Log {

  private static boolean ACTIVE = Version
      .getVersion()
      .contains("-snapshot");

  public static void log(String s, Object... args) {
    if (ACTIVE) {
      System.out.printf(s + "%n", args);
    }
  }
}
