package com.mageddo.csv2jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

public class Csv2JdbcExecutor {

  public static final int HEADER_COUNT = 1;

  private final Connection connection;
  private final CopyCsvStatement csvStm;

  public Csv2JdbcExecutor(Connection conn, String sql) {
    this.connection = conn;
    this.csvStm = Csv2JdbcConverter.of(sql);
  }

  public int execute() throws SQLException {
    try {
      switch (csvStm.getCommand()) {
        case FROM:
          return this.loadCsvIntoTable();
        case TO:
          return this.extractQueryToCsv();
        default:
          throw new UnsupportedOperationException(String.format("invalid option: %s", this.csvStm.getCommand()));
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private int extractQueryToCsv() throws SQLException {
    try {
      CsvTableDaos.streamSelect(this.connection, this.csvStm.getExtractSql(), (rs) -> {
        try (final CSVPrinter printer = this.createCsvPrinter()) {
          printer.printRecords(rs, true);
        }
      });
      return Files.countLines(this.csvStm.getFile()) - HEADER_COUNT;
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }

  private int loadCsvIntoTable() throws SQLException {
    try (final CSVParser csvParser = createCsvParser()) {

      final List<String> cols = CsvTableDaos.buildColNames(
          this.connection, this.csvStm.getCols(), csvParser.getHeaderNames(), this.csvStm.getTableName()
      );

      this.createTableIfNeedled(cols);

      final CsvTableDaoStrategy dao = CsvWriterFactory.create(this.connection);
      return dao.insertData(this.connection, csvParser, this.csvStm, cols);

    } catch (IOException e) {
      throw new SQLException(e);
    }
  }
  void createTableIfNeedled(List<String> cols) throws SQLException {
    if (this.csvStm.mustCreateTable()) {
      CsvTableDaos.createTable(this.connection, this.csvStm.getTableName(), cols);
    }
  }

  CSVParser createCsvParser() throws IOException {
    final CSVFormat csvFormat = getCsvFormat();
    return CSVParser.parse(this.csvStm.getFile(), this.csvStm.getCharset(), csvFormat);
  }

  private CSVPrinter createCsvPrinter() throws IOException {
    return this.getCsvFormat()
               .builder()
               .build()
               .print(this.csvStm.getFile(), this.csvStm.getCharset())
        ;
  }

  private CSVFormat getCsvFormat() {
    return CSVFormat.Builder
        .create(CSVFormat.DEFAULT)
        .setHeader()
        .setSkipHeaderRecord(true)
        .setDelimiter(this.csvStm.getDelimiter())
        .build();
  }

}
