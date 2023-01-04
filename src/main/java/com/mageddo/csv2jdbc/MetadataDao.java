package com.mageddo.csv2jdbc;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetadataDao {
  @SneakyThrows
  public static List<Column> findColumns(Connection c, String table)  {
    final ResultSet rs = c.getMetaData().getColumns(null, null, table, null);
    final List<Column> cols = new ArrayList<>();
    while (rs.next()) {
      cols.add(new Column(rs.getString("COLUMN_NAME"), Integer.parseInt(rs.getString("DATA_TYPE"))));
//      cols.add(Types.UnaryVisitor);
    }
    return cols;
  }

  public static Map<String, Column> findColumnsMap(Connection c, String table){
    return findColumns(c, table)
        .stream()
        .collect(Collectors.toMap(it -> it.getName().toLowerCase(), Function.identity()))
        ;
  }

  public static List<String> findColumnsNames(Connection c, String table){
    return findColumns(c, table)
        .stream()
        .map(Column::getName)
        .collect(Collectors.toList());
  }
}
