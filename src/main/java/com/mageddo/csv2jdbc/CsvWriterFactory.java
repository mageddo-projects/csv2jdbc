package com.mageddo.csv2jdbc;

import java.sql.Connection;

import com.mageddo.commons.jdbc.DB;
import com.mageddo.commons.jdbc.DBUtils;

public class CsvWriterFactory {
  public static CsvTableDaoStrategy create(Connection c) {
    final DB db = DBUtils.discoverDB(c);
    if (db.getName().equals("redshift")) {
      return new CsvTableDaoStrategyRawBatch();
    }
    return new CsvTableDaoStrategyBatch();
  }
}
