package com.mageddo.csv2jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CsvTableDaoStrategyBatch implements CsvTableDaoStrategy {
  @Override
  public int insertData(
      Connection connection, CopyCsvStatement csvStm, List<CSVRecord> records, List<String> cols
  ) {
    return CsvTableDaos.insertData(connection, csvStm, records, cols);
  }

  @Override
  public int insertData(
      Connection connection, CSVParser csvParser, CopyCsvStatement csvStm, List<String> cols
  ) {
    int i = 0;
    final int bufSize = 512;
    final List<CSVRecord> buff = new ArrayList<>(bufSize);
    for (CSVRecord record : csvParser) {
      i++;
      buff.add(record);
      if (i % bufSize == 0) {
        this.insertData(connection, csvStm, buff, cols);
        buff.clear();
      }
    }
    if (!buff.isEmpty()) {
      this.insertData(connection, csvStm, buff, cols);
    }
    return i;
  }
}
