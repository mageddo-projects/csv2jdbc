package com.mageddo.csv2jdbc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class Files {
  public static int countLines(Path p) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(p.toFile()))) {
      int lines = 0;
      while (reader.readLine() != null) {
        lines++;
      }
      return lines;
    }
  }
}
