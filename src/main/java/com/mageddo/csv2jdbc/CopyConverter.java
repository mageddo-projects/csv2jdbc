package com.mageddo.csv2jdbc;

import com.mageddo.csv2jdc.antlr.Csv2JdbcLexer;
import com.mageddo.csv2jdc.antlr.Csv2JdbcParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;

import java.util.List;
import java.util.stream.Collectors;

public class CopyConverter {
  public static Csv2JdbcParser of(String sql) {
    return new Csv2JdbcParser(new CommonTokenStream(new Csv2JdbcLexer(CharStreams.fromString(sql))));
  }

  public static void main(String[] args) {
    final Csv2JdbcParser parser = of("CSV2JCOPY   TTORECORD  (A, C, D) FROM 'tmp.csv' WITH CSV HEADER");

    final Csv2JdbcParser.CopystmtContext stm = parser
        .csv2j()
        .stmt()
        .copystmt();

    final List<String> options = stm
        .copy_options()
        .copy_opt_list()
        .copy_opt_item()
        .stream()
        .map(RuleContext::getText)
        .collect(Collectors.toList());

    final List<String> tableCols = stm
        .opt_column_list()
        .columnlist()
        .columnElem()
        .stream()
        .map(RuleContext::getText)
        .collect(Collectors.toList())
        ;

    System.out.println(stm.qualified_name().getText());
    System.out.println(tableCols);
    System.out.println(stm.copy_file_name().getText());
    System.out.println(options);
//    System.out.println(parser.csv2j().table().columnName().get(0).getText());
//    System.out.println(parser.csv2j().table().tableName().getText());

  }
}
