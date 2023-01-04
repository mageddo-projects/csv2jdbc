package com.mageddo.csv2jdbc;

import java.lang.reflect.InvocationTargetException;

public class Reflections {
  public static <T> T createInstance(String clazz) {
    try {
      return (T) createInstance(Class.forName(clazz));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T createInstance(Class<T> clazz) {
    try {
      return clazz
          .getDeclaredConstructor()
          .newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
