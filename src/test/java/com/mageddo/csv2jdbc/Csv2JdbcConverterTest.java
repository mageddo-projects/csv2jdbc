package com.mageddo.csv2jdbc;

import com.mageddo.csv2jdbc.CopyCsvStatement.Command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Csv2JdbcConverterTest {

  @Test
  void mustParseCsvLoadToTableStmt(){

    // arrange
    final var stmt = """
        CSV2J COPY FRUIT_TABLE FROM '/tmp/fruit.csv' WITH HEADER DELIMITER ';' CSV
        """;

    // act
    final var csvStatement = Csv2JdbcConverter.of(stmt);

    // assert
    assertEquals(Command.FROM, csvStatement.getCommand());
    assertFalse(csvStatement.mustCreateTable());
    assertEquals(';', csvStatement.getDelimiter());
    assertEquals("FRUIT_TABLE", csvStatement.getTableName());
    assertEquals("[]", csvStatement.getCols().toString());
    assertEquals("/tmp/fruit.csv", String.valueOf(csvStatement.getFile()));
    assertEquals("UTF-8", csvStatement.getCharset().displayName());
    assertTrue(csvStatement.hasHeader());

  }


  @Test
  void mustParseCols(){

    // arrange
    final var stmt = """
        CSV2J COPY FRUIT_TABLE (IDT_FRUIT_TABLE, DAT_CREATED, FLG_ACTIVE)
        FROM '/tmp/fruit.csv'
        WITH HEADER DELIMITER ';' CSV
        """;

    // act
    final var csvStatement = Csv2JdbcConverter.of(stmt);

    // assert
    assertEquals(Command.FROM, csvStatement.getCommand());
    assertEquals("[IDT_FRUIT_TABLE, DAT_CREATED, FLG_ACTIVE]", csvStatement.getCols().toString());
  }

  @Test
  void mustParseFromTableToFileStmt(){

    // arrange
    final var stmt = """
        CSV2J COPY (
          SELECT
            IDT_FRUIT_TABLE, DAT_CREATED, FLG_ACTIVE
          FROM FRUIT_TABLE
        )
        TO '/tmp/fruit.csv'
        WITH HEADER DELIMITER ';' CSV
        """;

    // act
    final var csvStatement = Csv2JdbcConverter.of(stmt);

    // assert
    assertEquals(Command.TO, csvStatement.getCommand());
    assertEquals(
        """
            SELECT
                IDT_FRUIT_TABLE, DAT_CREATED, FLG_ACTIVE
              FROM FRUIT_TABLE
            """.trim(),
        csvStatement.getExtractSql().trim()
    );
    assertEquals(';', csvStatement.getDelimiter());
    assertNull(csvStatement.getTableName());
    assertFalse(csvStatement.mustCreateTable());
    assertEquals("[]", csvStatement.getCols().toString());
    assertEquals("/tmp/fruit.csv", String.valueOf(csvStatement.getFile()));
    assertEquals("UTF-8", csvStatement.getCharset().displayName());
    assertTrue(csvStatement.hasHeader());
  }
}
