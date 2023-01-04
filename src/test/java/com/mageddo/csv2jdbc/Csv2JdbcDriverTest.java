package com.mageddo.csv2jdbc;

import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Csv2JdbcDriverTest {

  @Test
  void mustConnectAndDelegateSelectFromH2UsingVanillaJava() throws Exception {

    // arrange
    Class.forName(Csv2JdbcDriver.class.getName());

    // act
    final var conn = DriverManager.getConnection(
        "jdbc:csv2jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1?delegateDriverClassName=org.h2.Driver",
        "SA",
        ""
    );

    final var stm = conn.prepareStatement("select 9 AS ID");
    final var rs = stm.executeQuery();

    // assert
    try (stm; rs) {
      assertTrue(rs.next());
      assertEquals(9, rs.getInt("ID"));
    }

  }

}
