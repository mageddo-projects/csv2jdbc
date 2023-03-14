package com.mageddo.csv2jdbc;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Csv2JdbcDriverDynamicFileTest {

  public static final String JDBC_URL = "jdbc:csv2jdbc:h2:mem:testdb;" + "DB_CLOSE_DELAY=-1" +
      "?delegateDriverClassName=org.h2.Driver";

  static {
    try {
      Class.forName(Csv2JdbcDriver.class.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void mustCreatePath(@TempDir Path tempDir) throws Exception {
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("tmp/csv.csv");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1 AS ID, 10.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION
                UNION ALL
                SELECT 2 AS ID, 7.50 AS AMOUNT, TIMESTAMP '2023-01-31 21:59:58.987' AS DAT_CREATION
              ) TO '%s' WITH CSV HEADER
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    assertTrue(Files.isDirectory( tempDir.resolve("tmp") ) );

    assertEquals("""
        ID,AMOUNT,DAT_CREATION
        1,10.99,2022-01-31 23:59:58.987
        2,7.50,2023-01-31 21:59:58.987
        """.replaceAll("\n", "\r\n"), Files.readString(csvFile));

  }

  @Test
  void mustCreatePathWithCurrentDate(@TempDir Path tempDir) throws Exception {
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS/csv.csv");
    final var checkPath = String.format( "%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", Calendar.getInstance() );

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1 AS ID, 10.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION
                UNION ALL
                SELECT 2 AS ID, 7.50 AS AMOUNT, TIMESTAMP '2023-01-31 21:59:58.987' AS DAT_CREATION
              ) TO '%s' WITH CSV HEADER
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    assertTrue(Files.isDirectory( tempDir.resolve(checkPath) ) );

    final var csvFileCheck = tempDir.resolve(checkPath + "/csv.csv");

    assertEquals("""
        ID,AMOUNT,DAT_CREATION
        1,10.99,2022-01-31 23:59:58.987
        2,7.50,2023-01-31 21:59:58.987
        """.replaceAll("\n", "\r\n"), Files.readString(csvFileCheck));

  }

  @Test
  void mustFileNameByColumn(@TempDir Path tempDir) throws Exception {
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv_{ID}.csv");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1 AS ID, 10.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION
                UNION ALL
                SELECT 2 AS ID, 7.50 AS AMOUNT, TIMESTAMP '2023-01-31 21:59:58.987' AS DAT_CREATION
              ) TO '%s' WITH CSV HEADER
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    assertEquals(2, Files.list( tempDir ).count() );

    final var csvFileFirst = tempDir.resolve("csv_1.csv");
    assertEquals("""
        ID,AMOUNT,DAT_CREATION
        1,10.99,2022-01-31 23:59:58.987
        """.replaceAll("\n", "\r\n"), Files.readString(csvFileFirst));

    final var csvFileSecond = tempDir.resolve("csv_2.csv");
    assertEquals("""
        ID,AMOUNT,DAT_CREATION
        2,7.50,2023-01-31 21:59:58.987
        """.replaceAll("\n", "\r\n"), Files.readString(csvFileSecond));
  }

}
