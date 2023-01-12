package com.mageddo.csv2jdbc;

import java.sql.SQLException;

public class Csv2JdbcPreparedStatement extends NopPreparedStatement {

  private final Csv2JdbcExecutor executor;

  private int updatedCount;

  public Csv2JdbcPreparedStatement(Csv2JdbcExecutor executor) {
    this.executor = executor;
  }

  @Override
  public boolean execute() throws SQLException {
    this.executeUpdate();
    return true;
  }

  @Override
  public int executeUpdate() throws SQLException {
    return this.updatedCount = this.executor.execute();
  }

  @Override
  public int getUpdateCount(){
    return this.updatedCount;
  }

  @Override
  public void close() throws SQLException {

  }

  interface Consumer<T> {
    void accept(T o) throws Exception;
  }
}
