package com.mageddo.csv2jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mageddo.commons.jdbc.UncheckedSQLException;
import com.mageddo.csv2jdbc.Csv2JdbcPreparedStatement.Consumer;

import org.apache.commons.csv.CSVRecord;

public class CsvTableDaos {

  public static final String PARAM_SEPARATOR = ",";

  public static void createTable(Connection connection, String tableName, List<String> cols)
      throws SQLException {
    final String sql = String.format("CREATE TABLE %s (\n %s \n)", tableName, buildColDDL(cols));
    try (PreparedStatement stm = connection.prepareStatement(sql)) {
      stm.executeUpdate();
    }
  }

  public static int streamSelect(Connection conn, String sql, Consumer<ResultSet> c)
      throws Exception {
    try (PreparedStatement stm = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY
    )) {
      stm.setFetchSize(1024);
      try (final ResultSet rs = stm.executeQuery()) {
        c.accept(rs);
        return rs.getRow();
      }
    }
  }

  public static int rawInsertData(
      Connection connection, CopyCsvStatement csvStm, List<CSVRecord> records, List<String> cols
  ) {
    final long start = System.currentTimeMillis();
    final StringBuilder sql = new StringBuilder(String.format(
        "INSERT INTO %s (%s) VALUES", csvStm.getTableName(), buildColNamesStr(cols)
    ));
    try (Statement stm = connection.createStatement()) {
      System.out.println();
      for (CSVRecord r : records) {
        sql.append('(');
        sql.append(toValues(r));
        sql.append(')');
        sql.append(',');
      }
      sql.deleteCharAt(sql.length() - 1);
      int n = stm.executeUpdate(sql.toString());
      Log.log(
          "m=rawInsertData, time=%d, n=%d, sqlLen=%d",
          System.currentTimeMillis() - start, n, sql.length()
      );
      return n;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  private static String toValues(CSVRecord r) {
    return r
        .stream()
        .map(it -> String.format("'%s'", it))
        .collect(Collectors.joining(PARAM_SEPARATOR))
        ;
  }

  public static int insertData(
      Connection connection, CopyCsvStatement csvStm, List<CSVRecord> records, List<String> cols
  ) {

    final long start = System.currentTimeMillis();
    final String sql = String.format(
        "INSERT INTO %s (%s) VALUES (%s)",
        csvStm.getTableName(),
        buildColNamesStr(cols),
        buildBinds(cols)
    );
    try (PreparedStatement stm = connection.prepareStatement(sql)) {

      for (CSVRecord r : records) {
        int i = 1;
        for (String colVal : r) {
          stm.setString(i++, colVal);
        }
        stm.addBatch();
      }

      final int affected = IntStream
          .of(stm.executeBatch())
          .sum();
      Log.log(
          "m=batchInsertData, time=%d, n=%d, sqlLen=%d",
          System.currentTimeMillis() - start, affected, sql.length()
      );
      return affected;
    } catch (SQLException e) {
      throw new UncheckedSQLException(e);
    }
  }

  protected static List<String> buildColNames(Connection connection, List<String> cols,
      List<String> headerNames,
      String table) {
    if (cols != null && !cols.isEmpty()) {
      return cols;
    }
    if (headerNames != null && !headerNames.isEmpty()) {
      return headerNames
          .stream()
          .map(CsvTableDaos::sanitize)
          .collect(Collectors.toList());
    }
    return MetadataDao.findColumnsNames(connection, table);
  }

  static String buildColNamesStr(List<String> cols) {
    return String.join(PARAM_SEPARATOR, cols);
  }

  static String buildBinds(List<String> params) {
    return params
        .stream()
        .map(it -> "?")
        .collect(Collectors.joining(PARAM_SEPARATOR));
  }

  private static String buildColDDL(List<String> cols) {
    return cols.
        stream()
        .map(col -> String.format("%s VARCHAR(%d)", col, 1000))
        .collect(Collectors.joining(
            ",\n",
            "",
            ""
        ));
  }

  private static String sanitize(String s) {
    return s
        .replaceAll("^\\W+", "c_")
        .replaceAll("\\W+", "_")
        ;
  }

}
