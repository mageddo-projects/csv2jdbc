package com.mageddo.csv2jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;

public class Csv2JdbcConnection extends ProxiedConnection {
  public Csv2JdbcConnection(Connection delegate) {
    super(delegate);
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {

    final boolean importCsv = sql
        .trim()
        .toUpperCase(Locale.ENGLISH)
        .startsWith("IMPORT CSV");

    if(importCsv){
      System.out.println("Importing csv!!!!");
      throw new UnsupportedOperationException("Csv import not implemented!");
    }

    return super.prepareStatement(sql);
  }
}
