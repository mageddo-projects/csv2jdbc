package com.mageddo.csv2jdbc;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Column {
  String name;
  int type;
}
