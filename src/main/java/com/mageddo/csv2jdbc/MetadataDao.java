package com.mageddo.csv2jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.SneakyThrows;

public class MetadataDao {
  @SneakyThrows
  public static List<Column> findColumns(Connection c, String table) {
    List<Column> first = toColumns(toRs(c, table.toUpperCase(Locale.ENGLISH)));
    if (first.isEmpty()) {
      return toColumns(toRs(c, table.toLowerCase(Locale.ENGLISH)));
    }
    return first;
  }

  private static ResultSet toRs(Connection c, String table) throws SQLException {
    return c.getMetaData().getColumns(null, null, table, null);
  }

  public static Map<String, Column> findColumnsMap(Connection c, String table) {
    return columnsToMap(findColumns(c, table));
  }

  public static List<String> findColumnsNames(Connection c, String table) {
    return findColumns(c, table)
        .stream()
        .map(Column::getName)
        .collect(Collectors.toList());
  }

  public static Map<String, Column> findColumnsMap(ResultSetMetaData metaData) throws SQLException {
    return columnsToMap(toColumns(metaData));
  }

  private static Map<String, Column> columnsToMap(List<Column> columns) {
    return columns
        .stream()
        .collect(Collectors.toMap(it -> it.getName().toLowerCase(), Function.identity()))
        ;
  }

  private static List<Column> toColumns(ResultSet rs) throws SQLException {
    final List<Column> cols = new ArrayList<>();
    while (rs.next()) {
      cols.add(new Column(rs.getString("COLUMN_NAME"), Integer.parseInt(rs.getString("DATA_TYPE"))));
//      cols.add(Types.UnaryVisitor);
    }
    return cols;
  }

  private static List<Column> toColumns(ResultSetMetaData metaData) throws SQLException {
    return IntStream
        .range(1, metaData.getColumnCount() + 1)
        .boxed()
        .map(i -> {
          try {
            return new Column(metaData.getColumnLabel(i), metaData.getColumnType(i));
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());
  }
}
