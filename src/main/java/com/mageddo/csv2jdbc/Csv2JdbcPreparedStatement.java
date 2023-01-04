package com.mageddo.csv2jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Csv2JdbcPreparedStatement extends NopPreparedStatement {

  private CopyCsvStatement csvStm;
  private Connection connection;

  private int updatedCount;

  public Csv2JdbcPreparedStatement(CopyCsvStatement csvStm, Connection connection) {
    this.csvStm = csvStm;
    this.connection = connection;
  }

  @Override
  public boolean execute() throws SQLException {
    this.executeUpdate();
    return true;
  }

  @Override
  public int executeUpdate() throws SQLException {

    try (final CSVParser csvParser = createCsvParser()) {

      final List<String> cols = CsvTableDao.buildColNames(
          this.connection, this.csvStm.getCols(), csvParser.getHeaderNames(), this.csvStm.getTableName()
      );

      this.createTableIfNeedled(cols);

      final int bufSize = 128;
      final List<CSVRecord> buff = new ArrayList<>(bufSize);
      int i = 0;

      for (final CSVRecord record : csvParser) {
        i++;
        buff.add(record);
        if(buff.size() % bufSize == 0){
          CsvTableDao.insertData(this.connection, this.csvStm, buff, cols);
          buff.clear();
        }
      }

      if(!buff.isEmpty()){
        CsvTableDao.insertData(this.connection, this.csvStm, buff, cols);
      }
      return this.updatedCount = i;
    } catch (IOException e) {
      throw new SQLException(e);
    }

    // todo parsear csv
    // todo carregar na base
    // todo fazer repackage do apache commons csv e do antlr para nao ter problema com outras libs, procurar plugin para
    // isso estilo maven shade

  }

  @Override
  public int getUpdateCount() throws SQLException {
    return this.updatedCount;
  }

  @Override
  public void close() throws SQLException {

  }

  void createTableIfNeedled(List<String> cols) throws SQLException {
    if (this.csvStm.mustCreateTable()) {
      CsvTableDao.createTable(this.connection, this.csvStm.getTableName(), cols);
    }
  }

  CSVParser createCsvParser() throws IOException {
    final CSVFormat csvFormat = CSVFormat.Builder
        .create(CSVFormat.DEFAULT)
        .setHeader()
        .setSkipHeaderRecord(true)
        .setDelimiter(this.csvStm.getDelimiter())
        .build();
    return CSVParser.parse(this.csvStm.getFile(), this.csvStm.getCharset(), csvFormat);
  }
}
