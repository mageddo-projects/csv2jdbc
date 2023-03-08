package com.mageddo.csv2jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProxiedResultSet extends DelegateResultSet {
  private int count = 0;

  public ProxiedResultSet(ResultSet delegate) {
    super(delegate);
  }

  @Override
  public boolean next() throws SQLException {
    count++;
    return super.next();
  }

  @Override
  public int getRow() throws SQLException {
    return count;
  }
}
