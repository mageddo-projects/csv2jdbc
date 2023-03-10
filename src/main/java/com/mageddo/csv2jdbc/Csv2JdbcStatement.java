package com.mageddo.csv2jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.mageddo.csv2jdbc.Csv2JdbcConnection.isCsv2JCopy;

public class Csv2JdbcStatement extends ProxiedStatement {

  public Csv2JdbcStatement(Statement delegate) {
    super(delegate);
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    // todo
    return super.executeQuery(sql);
  }

  @Override
  public int executeUpdate(String sql) throws SQLException {
    if (isCsv2JCopy(sql)) {
      return new Csv2JdbcExecutor(this.getConnection(), sql).execute();
    }
    return super.executeUpdate(sql);
  }

  @Override
  public boolean execute(String sql) throws SQLException {
    if(isCsv2JCopy(sql)){
      return new Csv2JdbcExecutor(this.getConnection(), sql).execute() > 0;
    }
    return super.execute(sql);
  }

  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    // todo
    return super.executeUpdate(sql, autoGeneratedKeys);
  }

  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    // todo
    return super.executeUpdate(sql, columnIndexes);
  }

  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    // todo
    return super.executeUpdate(sql, columnNames);
  }

  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    // todo
    return super.execute(sql, autoGeneratedKeys);
  }

  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    // todo
    return super.execute(sql, columnIndexes);
  }

  @Override
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    // todo
    return super.execute(sql, columnNames);
  }
}
