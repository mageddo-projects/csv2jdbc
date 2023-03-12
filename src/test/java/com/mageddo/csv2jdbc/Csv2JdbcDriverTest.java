package com.mageddo.csv2jdbc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.antlr.v4.runtime.CharStreams;
import org.h2.util.IOUtils;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Csv2JdbcDriverTest {

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
  void mustCopyCsvToTableUsingStatement(@TempDir Path tempDir) throws Exception {

    // arrange

    // act
    final var conn = DriverManager.getConnection(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");
    copy("/data/csv2jdbc-driver-test/people.csv", csvFile);

    final var stm = conn.createStatement();
    final var executed = stm.execute(
        String.format("CSV2J COPY MOVS_TX1 FROM '%s' WITH CSV HEADER CREATE_TABLE", csvFile));

    // assert
    assertTrue(executed);
  }

  @Test
  void mustImportCsvToTable(@TempDir Path tempDir) throws Exception {

    // arrange
    final var records = 9;
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");
    copy("/data/csv2jdbc-driver-test/people.csv", csvFile);

    // act
    jdbi.useHandle(h -> {
      final var r = h
          .createUpdate(String.format(
              "CSV2J COPY MOVS FROM '%s' WITH CSV HEADER CREATE_TABLE DELIMITER ','",
              csvFile
          ))
          .execute();
      assertEquals(records, r);
    });

    // assert

    jdbi.useHandle(h -> {
      final var data = h.createQuery("SELECT * FROM MOVS")
          .mapToMap();
      data.forEach(System.out::println);
      assertEquals(records, data.stream()
          .count());
    });


  }

  @Test
  void mustImportCsvToTableCaseInsensitive(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");
    copy("/data/csv2jdbc-driver-test/people.csv", csvFile);

    // act
    // assert
    jdbi.useHandle(h -> {
      final var r = h.createUpdate(String.format(
              "CSV2J COPY TABLE_xPtO FROM '%s' WITH CSV HEADER CREATE_TABLE DELIMITER ','",
              csvFile
          ))
          .execute();
      assertEquals(9, r);
    });

  }

  @Test
  void mustExtractQueryToCsv(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");

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

    assertEquals("""
        ID,AMOUNT,DAT_CREATION
        1,10.99,2022-01-31 23:59:58.987
        2,7.50,2023-01-31 21:59:58.987
        """.replaceAll("\n", "\r\n"), Files.readString(csvFile));
  }

  @Test
  void mustExtractQueryToCsvWithOutHeader(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1 AS ID, 10.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION
                UNION ALL
                SELECT 2 AS ID, 7.50 AS AMOUNT, TIMESTAMP '2023-01-31 21:59:58.987' AS DAT_CREATION
              ) TO '%s' WITH CSV
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    assertEquals("""
        1,10.99,2022-01-31 23:59:58.987
        2,7.50,2023-01-31 21:59:58.987
        """.replaceAll("\n", "\r\n"), Files.readString(csvFile));
  }

  @Test
  void mustExtractQueryToCsvZIP(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1 AS ID, 10.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION
                UNION ALL
                SELECT 2 AS ID, 7.50 AS AMOUNT, TIMESTAMP '2023-01-31 21:59:58.987' AS DAT_CREATION
              ) TO '%s' WITH CSV HEADER ZIP
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    ZipFile zip = new ZipFile( csvFile + ".zip" );
    ZipEntry entry = zip.getEntry(csvFile.getFileName().toString() );
    String readFromZip = CharStreams.fromStream( zip.getInputStream( entry ) ).toString();
    zip.close();

    assertEquals("""
        ID,AMOUNT,DAT_CREATION
        1,10.99,2022-01-31 23:59:58.987
        2,7.50,2023-01-31 21:59:58.987
        """.replaceAll("\n", "\r\n"), readFromZip );
  }

  @Test
  void mustExtractQueryToCsvGZIP(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1 AS ID, 10.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION
                UNION ALL
                SELECT 2 AS ID, 7.50 AS AMOUNT, TIMESTAMP '2023-01-31 21:59:58.987' AS DAT_CREATION
              ) TO '%s' WITH CSV HEADER GZIP
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    InputStream fileStream = new FileInputStream(csvFile + ".gzip");
    InputStream gzipStream = new GZIPInputStream(fileStream);
    String readFromGZip = CharStreams.fromStream( gzipStream ).toString();
    fileStream.close();

    assertEquals("""
        ID,AMOUNT,DAT_CREATION
        1,10.99,2022-01-31 23:59:58.987
        2,7.50,2023-01-31 21:59:58.987
        """.replaceAll("\n", "\r\n"), readFromGZip );
  }

  @Test
  void mustExtractQueryToCsvUsingPT_BR(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1234567890 AS ID, 1234567890.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION, DATE '2023-03-11' as DAT_BORN, TIME '21:47:01' as TIME_BORN
                UNION ALL
                SELECT 1000000 AS ID, 1000.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION, DATE '1978-04-16' as DAT_BORN, TIME '23:35:00' as TIME_BORN
              ) TO '%s' WITH CSV HEADER LANGUAGE 'pt-BR'
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    assertEquals("""
       ID,AMOUNT,DAT_CREATION,DAT_BORN,TIME_BORN
       1.234.567.890,"1.234.567.890,99",31/01/2022 23:59:58,11/03/2023,21:47:01
       1.000.000,"1.000,99",31/01/2022 23:59:58,16/04/1978,23:35:00
        """.replaceAll("\n", "\r\n"), Files.readString(csvFile));
  }

  @Test
  void mustExtractQueryToCsvUsingEN_US(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1234567890 AS ID, 1234567890.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION, DATE '2023-03-11' as DAT_BORN, TIME '21:47:01' as TIME_BORN
                UNION ALL
                SELECT 1000000 AS ID, 1000.99 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION, DATE '1978-04-16' as DAT_BORN, TIME '23:35:00' as TIME_BORN
              ) TO '%s' WITH CSV HEADER LANGUAGE 'en-US'
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    assertEquals("""
        ID,AMOUNT,DAT_CREATION,DAT_BORN,TIME_BORN
        "1,234,567,890","1,234,567,890.99","1/31/22, 11:59:58 PM",3/11/23,9:47:01 PM
        "1,000,000","1,000.99","1/31/22, 11:59:58 PM",4/16/78,11:35:00 PM
        """.replaceAll("\n", "\r\n"), Files.readString(csvFile));
  }


  @Test
  void mustExtractQueryToCsvUsingPT_BR_with_CustomFormat(@TempDir Path tempDir) throws Exception {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");
    final var csvFile = tempDir.resolve("csv.csv");

    // act
    // assert
    jdbi.useHandle(h -> {
      final var updated = h.createUpdate(String.format("""
              CSV2J COPY (
                SELECT 1234567890 AS ID, 1234567890.995 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58.987' AS DAT_CREATION, DATE '2023-03-11' as DAT_BORN, TIME '21:47:01.001' as TIME_BORN
                UNION ALL
                SELECT 1 AS ID, 1.12345 AS AMOUNT, TIMESTAMP '2022-01-31 23:59:58' AS DAT_CREATION, DATE '1978-04-16' as DAT_BORN, TIME '23:35:00' as TIME_BORN
              ) TO '%s' WITH CSV HEADER LANGUAGE 'pt-BR'
               DATETIMEFORMAT 'yyyy-MM-dd HH:mm:ss.SSSXXX'
               DATEFORMAT 'yyyy-MM-dd'
               TIMEFORMAT 'HH:mm:ss.SSSXXX'
               NUMBERFORMAT '#00'
               DECIMALFORMAT '#00.00'
              """, csvFile))
          .execute();
      assertEquals(2, updated);
    });

    assertEquals("""
        ID,AMOUNT,DAT_CREATION,DAT_BORN,TIME_BORN
        1234567890,"1234567891,00",2022-01-31 23:59:58.987-03:00,2023-03-11,21:47:01.001-03:00
        01,"01,12",2022-01-31 23:59:58.000-03:00,1978-04-16,23:35:00.000-03:00
        """.replaceAll("\n", "\r\n"), Files.readString(csvFile));
  }


  private void copy(String source, Path target) throws IOException {
    final InputStream in = getClass().getResourceAsStream(source);
    final OutputStream out = Files.newOutputStream(target);
    try (in; out) {
      IOUtils.copy(in, out);
    }
  }

}
