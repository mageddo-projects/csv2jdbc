package com.mageddo.csv2jdbc;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

public class CsvTableDaoStrategyRawBatch implements CsvTableDaoStrategy {
  @Override
  public void insertData(Connection connection, CopyCsvStatement csvStm, List<CSVRecord> records, List<String> cols) {

  }
}
