package com.mageddo.csv2jdbc;

import org.apache.commons.csv.CSVRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvTableDao {

  public static final String PARAM_SEPARATOR = ", ";

  public static void createTable(Connection connection, String tableName, List<String> cols) throws SQLException {
    final String sql = String.format("CREATE TABLE %s (\n %s \n)", tableName, buildColDDL(cols));
    try (PreparedStatement stm = connection.prepareStatement(sql)) {
      stm.executeUpdate();
    }
  }

  public static void insertData(
      Connection connection, CopyCsvStatement csvStm, List<CSVRecord> record, List<String> cols
  ) throws SQLException {

    final Map<String, Column> columns = MetadataDao.findColumnsMap(connection, csvStm.getTableName());

    final String sql = String.format(
        "INSERT INTO %s (%s) VALUES (%s)",
        csvStm.getTableName(),
        buildColNamesStr(cols),
        buildBinds(cols)
    );
    try (PreparedStatement stm = connection.prepareStatement(sql)) {
      for (CSVRecord r : record) {
        int colI = 1;
        for (String colVal : r) {
          final Column columnMetadata = columns.get(cols.get(colI - 1).toLowerCase());
          if(colVal == null){
            stm.setNull(colI++, columnMetadata.getType());
          } else {
            stm.setObject(colI++, colVal, columnMetadata.getType());
          }
        }
        stm.addBatch();
      }
      stm.executeBatch();
    }
  }

  protected static List<String> buildColNames(Connection connection, List<String> cols, List<String> headerNames, String table) {
    if (cols != null && !cols.isEmpty()) {
      return cols;
    }
    if (headerNames != null && !headerNames.isEmpty()) {
      return headerNames
          .stream()
          .map(CsvTableDao::sanitize)
          .collect(Collectors.toList());
    }
    return MetadataDao.findColumnsNames(connection, table);
  }

  static String buildColNamesStr(List<String> cols) {
    return String.join(PARAM_SEPARATOR, cols);
  }

  static String buildBinds(List<String> params) {
    return params.stream()
        .map(it -> "?")
        .collect(Collectors.joining(PARAM_SEPARATOR));
  }


  private static String buildColDDL(List<String> cols) {
    return cols.
        stream()
        .map(CsvTableDao::sanitize)
        .collect(Collectors.joining(" VARCHAR(255),\n", "", " VARCHAR(255)"));
  }

  private static String sanitize(String s) {
    return s
        .replaceAll("(^[^a-zA-Z])+", "c$1")
        .replaceAll("([^a-zA-Z]+$)", "c$1");
  }
}
