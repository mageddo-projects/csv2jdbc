package com.mageddo.csv2jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

import org.h2.util.IOUtils;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Csv2JdbcDriverTest {

  public static final String JDBC_URL =
      "jdbc:csv2jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1?delegateDriverClassName=org.h2.Driver";

  static {
    try {
      Class.forName(Csv2JdbcDriver.class.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void mustConnectAndDelegateSelectFromH2UsingVanillaJavaAndPreparedStatment() throws Exception {

    // arrange

    // act
    final var conn = DriverManager.getConnection(JDBC_URL, "SA", "");

    final var stm = conn.prepareStatement("select 9 AS ID");
    final var rs = stm.executeQuery();

    // assert
    try (stm; rs) {
      assertTrue(rs.next());
      assertEquals(9, rs.getInt("ID"));
    }

  }

  @Test
  void mustCopyCsvToTableUsingStatement() throws Exception {

    // arrange

    // act
    final var conn = DriverManager.getConnection(JDBC_URL, "SA", "");
    final var file = "/Users/elfreitas/Documents/csv/relatorio-contabil-usuario-2022-09.csv";

    final var stm = conn.createStatement();
    final var executed = stm.execute(String.format(
        "CSV2J COPY MOVS FROM '%s' WITH CSV HEADER CREATE_TABLE DELIMITER ';'",
        file
    ));

    // assert
    assertTrue(executed);
  }

  @Test
  void mustImportCsvToTable(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");
    final InputStream in = getClass().getResourceAsStream("/data/csv2jdbc-driver-test/people.csv");
    final OutputStream out = Files.newOutputStream(csvFile);
    try (in; out) {
      IOUtils.copy(in, out);
    }

    // act
    jdbi.useHandle(h -> {
      final var r = h
          .createUpdate(String.format(
              "CSV2J COPY MOVS FROM '%s' WITH CSV HEADER CREATE_TABLE DELIMITER ','",
              csvFile
          ))
          .execute();
      assertEquals(9, r);
    });

    // assert

    jdbi.useHandle(h -> {
      h
          .createQuery("SELECT * FROM MOVS").mapToMap()
          .forEach(o -> {
            System.out.println(o);
          });
    });


  }

  @Test
  void mustExtractQueryToCsv() {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h
          .createUpdate("""
              CSV2J COPY (
                SELECT 1 AS ID, 10.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION
                UNION ALL
                SELECT 2 AS ID, 7.50 AS AMOUNT, TIMESTAMP '2023-01-31 21:59:58.987' AS DAT_CREATION
              ) TO '/Users/elfreitas/Documents/test.csv' WITH CSV HEADER
              """
          )
          .execute();
      assertEquals(2, updated);
    });

    // todo assert file content

  }

}
