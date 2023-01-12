package com.mageddo.csv2jdbc;

public class Log {
  public static void log(String s, Object ... args){
    System.out.printf(s + "%n", args);
  }
}
