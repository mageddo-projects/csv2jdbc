package com.mageddo.csv2jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CsvTableDaoStrategyRawBatch implements CsvTableDaoStrategy {

  public static final int _16MB_IN_BYTES = 16 * 1024 * 1024;
  public static final int COL_METADATA_SIZE = 3; // duas aspas simples e uma virgula
  public static final int ROW_METADATA_SIZE = 3; // dois parenteses

  @Override
  public int insertData(
      Connection connection, CopyCsvStatement csvStm, List<CSVRecord> records, List<String> cols
  ) {
    return CsvTableDaos.rawInsertData(connection, csvStm, records, cols);
  }

  @Override
  public int insertData(
      Connection connection, CSVParser csvParser, CopyCsvStatement csvStm, List<String> cols
  ) {

    final int bufferSize = this.bufferSize();
    final AtomicInteger buffRemaining = new AtomicInteger(bufferSize);
    List<CSVRecord> buff = new ArrayList<>();
    int i = 0;
    for (final CSVRecord record : csvParser) {

      i++;
      final int recordSize = this.calcRecordSizeInBytes(record);
      buffRemaining.addAndGet(-recordSize);

      if (buffRemaining.get() <= 0) {
        this.insertData(connection, csvStm, buff, cols);
        buff.clear();
        buffRemaining.set(bufferSize - recordSize);
      }
      buff.add(record);
    }

    if (!buff.isEmpty()) {
      this.insertData(connection, csvStm, buff, cols);
    }
    return i;
  }

  private int bufferSize() {
    return Integer.parseInt(
        System.getProperty("csv2jdbc.buffSize", String.valueOf(_16MB_IN_BYTES))
    );
  }

  private int calcRecordSizeInBytes(CSVRecord record) {
    return record
        .toList()
        .stream()
        .mapToInt(it -> it.length() + COL_METADATA_SIZE)
        .sum() + ROW_METADATA_SIZE;
  }

}
