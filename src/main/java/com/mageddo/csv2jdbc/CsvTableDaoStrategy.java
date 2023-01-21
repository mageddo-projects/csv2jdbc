package com.mageddo.csv2jdbc;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public interface CsvTableDaoStrategy {
  int insertData( Connection connection, CopyCsvStatement csvStm, List<CSVRecord> records,
      List<String> cols);

  int insertData(Connection connection, CSVParser csvParser, CopyCsvStatement csvStm, List<String> cols);
}
