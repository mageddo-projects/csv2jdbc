package com.mageddo.csv2jdbc;

import java.time.LocalDateTime;

public class Log {

  private final static boolean ACTIVE = Version
      .getVersion()
      .contains("-snapshot");

  static {
    System.out.printf("log, logEnabled=%s, version=%s%n", ACTIVE, Version.getVersion());
  }

  public static void log(String s, Object... args) {
    if (ACTIVE) {
      System.out.printf(
          String.format(
              "%s %s%n",
              LocalDateTime.now(), s.replaceAll("\\{}", "%s")
          ),
          args
      );
    }
  }
}
