package com.mageddo.csv2jdbc;

import lombok.experimental.Delegate;

import java.sql.ResultSet;

public class DelegateResultSet implements ResultSet {

  @Delegate(types = ResultSet.class)
  final private ResultSet delegate;

  public DelegateResultSet(ResultSet delegate) {
    this.delegate = delegate;
  }

}
