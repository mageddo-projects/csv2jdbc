package com.mageddo.csv2jdbc;

import com.mageddo.csv2jdbc.CopyCsvStatement.Option;
import com.mageddo.csv2jdc.antlr.Csv2JdbcLexer;
import com.mageddo.csv2jdc.antlr.Csv2JdbcParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CopyConverter {
  private static Csv2JdbcParser parserOf(String sql) {
    return new Csv2JdbcParser(new CommonTokenStream(new Csv2JdbcLexer(CharStreams.fromString(sql))));
  }

  public static CopyCsvStatement of(String sql) {

    final Csv2JdbcParser parser = parserOf(sql);

    final Csv2JdbcParser.CopystmtContext stm = parser
        .csv2j()
        .stmt()
        .copystmt();

    final Map<String, Option> options = stm
        .copy_options()
        .copy_opt_list()
        .copy_opt_item()
        .stream()
        .map(it -> {
          switch (it.getChildCount()) {
            case 1:
              return new Option(it.getText());
            case 2:
              return new Option(it.getChild(0).getText(), clearStr(it.getChild(1)));
            default:
              throw new UnsupportedOperationException(String.valueOf(it.getChildCount()));
          }
        })
        .collect(Collectors.toMap(Option::getName, Function.identity()));

    final List<String> tableCols = stm.opt_column_list().columnlist() == null ? Collections.emptyList() : stm
        .opt_column_list()
        .columnlist()
        .columnElem()
        .stream()
        .map(RuleContext::getText)
        .collect(Collectors.toList());

    if (!options.containsKey(Option.CSV)) {
      throw new IllegalStateException("Must inform CSV option, ex: ... WITH CSV");
    }
    return CopyCsvStatement
        .builder()
        .cols(tableCols)
        .tableName(stm.qualified_name().getText())
        .file(Paths.get(clearStr(stm.copy_file_name())))
        .options(options)
        .build();

  }

  private static String clearStr(ParseTree o) {
    return o.getText().replaceAll("^'", "").replaceAll("'$", "");
  }
}
