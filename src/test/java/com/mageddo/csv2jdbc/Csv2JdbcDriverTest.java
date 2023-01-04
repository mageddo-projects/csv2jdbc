package com.mageddo.csv2jdbc;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Csv2JdbcDriverTest {

  public static final String JDBC_URL = "jdbc:csv2jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1?delegateDriverClassName=org.h2.Driver";

  static {
    try {
      Class.forName(Csv2JdbcDriver.class.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void mustConnectAndDelegateSelectFromH2UsingVanillaJava() throws Exception {

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
  void mustImportCsvToTable() {

    // arrange
    final var jdbi = Jdbi.create(JDBC_URL, "SA", "");

    // act
    jdbi.useHandle(h -> {
      final var r = h
          .createUpdate("CSV2J COPY MOVS FROM '/home/typer/.mageddo/ipca/ipca-series.csv' WITH CSV HEADER CREATE_TABLE DELIMITER ','")
          .execute();
      assertEquals(109, r);
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

}
