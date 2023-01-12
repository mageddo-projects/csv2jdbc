package com.mageddo.csv2jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ProxiedConnection implements Connection {

  final Connection delegate;

  public ProxiedConnection(Connection delegate) {
    this.delegate = delegate;
  }

  @Override
  public Statement createStatement() throws SQLException {
    info("m=createStatement");
    return new Csv2JdbcStatement(this.delegate.createStatement());
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    info("m=prepareStatement");
    return this.delegate.prepareStatement(sql);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    info("m=prepareCall");
    return this.delegate.prepareCall(sql);
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    info("m=nativeSQL");
    return this.delegate.nativeSQL(sql);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    info("m=getAutoCommit");
    return this.delegate.getAutoCommit();
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    info("m=setAutoCommit");
    this.delegate.setAutoCommit(autoCommit);
  }

  @Override
  public void commit() throws SQLException {
    info("m=commit");
    this.delegate.commit();
  }

  @Override
  public void rollback() throws SQLException {
    info("m=rollback");
    this.delegate.rollback();
  }

  @Override
  public void close() throws SQLException {
    info("m=close");
    this.delegate.close();
  }

  @Override
  public boolean isClosed() throws SQLException {
    info("m=isClosed");
    return this.delegate.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    info("m=getMetaData");
    return this.delegate.getMetaData();
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    info("m=isReadOnly");
    return this.delegate.isReadOnly();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    info("m=setReadOnly");
    this.delegate.setReadOnly(readOnly);
  }

  @Override
  public String getCatalog() throws SQLException {
    info("m=getCatalog");
    return this.delegate.getCatalog();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    info("m=setCatalog");
    this.delegate.setCatalog(catalog);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    info("m=getTransactionIsolation");
    return this.delegate.getTransactionIsolation();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    info("m=setTransactionIsolation");
    this.delegate.setTransactionIsolation(level);
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    info("m=getWarnings");
    return this.delegate.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    info("m=clearWarnings");
    this.delegate.clearWarnings();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    info("m=createStatement2");
    return this.delegate.createStatement(resultSetType, resultSetConcurrency);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    info("m=prepareStatement2");
    return this.delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    info("m=prepareCall2");
    return this.delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    info("m=getTypeMap");
    return this.delegate.getTypeMap();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    info("m=setTypeMap");
    this.delegate.setTypeMap(map);
  }

  @Override
  public int getHoldability() throws SQLException {
    info("m=getHoldability");
    return this.delegate.getHoldability();
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    info("m=setHoldability");
    this.delegate.setHoldability(holdability);
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    info("m=setSavepoint");
    return this.delegate.setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    info("m=setSavepoint");
    return this.delegate.setSavepoint(name);
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    info("m=rollback");
    this.delegate.rollback(savepoint);
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    info("m=releaseSavepoint");
    this.delegate.releaseSavepoint(savepoint);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    info("m=createStatement3");
    return this.delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    info("m=prepareStatement3");
    return this.delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    info("m=prepareCall3");
    return this.delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    info("m=prepareStatement4");
    return this.delegate.prepareStatement(sql, autoGeneratedKeys);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    info("m=prepareStatement5");
    return this.delegate.prepareStatement(sql, columnIndexes);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    info("m=prepareStatement6");
    return this.delegate.prepareStatement(sql, columnNames);
  }

  @Override
  public Clob createClob() throws SQLException {
    info("m=createClob");
    return this.delegate.createClob();
  }

  @Override
  public Blob createBlob() throws SQLException {
    info("m=createBlob");
    return this.delegate.createBlob();
  }

  @Override
  public NClob createNClob() throws SQLException {
    info("m=createNClob");
    return this.delegate.createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    info("m=createSQLXML");
    return this.delegate.createSQLXML();
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    info("m=isValid");
    return this.delegate.isValid(timeout);
  }

  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    info("m=setClientInfo");
    this.delegate.setClientInfo(name, value);
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    info("m=getClientInfo");
    return this.delegate.getClientInfo(name);
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    info("m=getClientInfo");
    return this.getClientInfo();
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    info("m=setClientInfo");
    this.delegate.setClientInfo(properties);
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    info("m=createArrayOf");
    return this.delegate.createArrayOf(typeName, elements);
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    info("m=createStruct");
    return this.delegate.createStruct(typeName, attributes);
  }

  @Override
  public String getSchema() throws SQLException {
    info("m=getSchema");
    return this.delegate.getSchema();
  }

  @Override
  public void setSchema(String schema) throws SQLException {
    info("m=setSchema");
    this.delegate.setSchema(schema);
  }

  @Override
  public void abort(Executor executor) throws SQLException {
    info("m=abort");
    this.delegate.abort(executor);
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    info("m=setNetworkTimeout");
    this.delegate.setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    info("m=getNetworkTimeout");
    return this.delegate.getNetworkTimeout();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    info("m=unwrap");
    return this.delegate.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    info("m=isWrapperFor");
    return this.delegate.isWrapperFor(iface);
  }

  public void info(String s) {
    System.out.println(s);
  }
}
