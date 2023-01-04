package com.mageddo.csv2jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * Syntax is inspired in https://www.postgresql.org/docs/current/sql-copy.html .
 *
 * <ol>
 *   <li><strong><code>,</code></strong> (comma), is the default delimiter</li>
 * </ol>
 *
 * Examples:
 *
 * <code><pre>
 *   -- loads a csv which has a header to the table filling table columns in the same order as the csv ; is the delimiter
 *   CSV2J COPY :tableName FROM :csvPath WITH HEADER DELIMITER ';' CSV
 *
 *   -- same as previous but the csv haven't a header
 *   CSV2J COPY :tableName FROM :csvPath WITH DELIMITER ';' CSV
 *
 *   -- loads a csv creating the dest table
 *   CSV2J COPY :tableName FROM :csvPath WITH DELIMITER ';' CSV CREATE_TABLE
 * </pre></code>
 */
public class CopyCsvStatement {
  public static PreparedStatement of(String sql, Connection conn) {

    throw new UnsupportedOperationException();
  }
}
