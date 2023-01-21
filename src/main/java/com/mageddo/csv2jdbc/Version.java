package com.mageddo.csv2jdbc;

import java.io.IOException;
import java.util.Properties;

public class Version {

  private static final Properties PROPS = new Properties();

  static {
    try {
      PROPS.load(Version.class.getResourceAsStream("/com/mageddo/csv2jdbc/info.properties"));
    } catch (IOException e) {
    }
  }

  public static String getVersion() {
    return PROPS.getProperty("version", "0.0.0");
  }

}
