package com.mageddo.csv2jdbc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Column {
  String name;
  int type;
}
