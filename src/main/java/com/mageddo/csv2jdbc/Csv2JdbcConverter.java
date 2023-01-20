package com.mageddo.csv2jdbc;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mageddo.antlr.PostgreSQLLexer;
import com.mageddo.antlr.PostgreSQLParser;
import com.mageddo.csv2jdbc.CopyCsvStatement.Command;
import com.mageddo.csv2jdbc.CopyCsvStatement.Option;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

public class Csv2JdbcConverter {

  public static CopyCsvStatement of(String sql) {
    final PostgreSQLParser parser = new PostgreSQLParser(new CommonTokenStream(new PostgreSQLLexer(
        CharStreams.fromString(sql)
    )));

    final PostgreSQLParser.CopycsvstmtContext stm = parser
        .stmt()
        .copycsvstmt();

    final Map<String, Option> options = stm
        .copy2csv_options()
        .copy2csv_opt_list()
        .copy2csv_opt_item()
        .stream()
        .map(it -> {
          switch (it.getChildCount()) {
            case 1:
              return new Option(it.getText());
            case 2:
              return new Option(it.getChild(0).getText(), clearStr(it.getChild(1), "'"));
            default:
              throw new UnsupportedOperationException(String.valueOf(it.getChildCount()));
          }
        })
        .collect(Collectors.toMap(Option::getName, Function.identity()));

    final List<String> tableCols = parseCols(stm);

    return CopyCsvStatement
        .builder()
        .command(Command.valueOf(Objects.firstNonNull(stm.copy_from(), stm.TO()).getText()))
        .file(Paths.get(clearStr(stm.copy_file_name(), "'")))
        .options(options)

        // from
        .tableName(parseTableName(stm))
        .cols(tableCols)

        // to
        .extractSql(Objects.mapOrNull(
            stm.preparablestmt(), it -> {
              final int start = it.getStart().getStartIndex();
              final int end = it.getStop().getStopIndex();
              return it
                  .selectstmt()
                  .getStart()
                  .getInputStream()
                  .getText(new Interval(start, end));
            }))
        .build()
        .validateIsCsv();

  }

  private static String parseTableName(PostgreSQLParser.CopycsvstmtContext stm) {
    return Optional
        .ofNullable(stm.qualified_name())
        .map(ParseTree::getText)
        .orElse(null)
        ;
  }

  private static List<String> parseCols(PostgreSQLParser.CopycsvstmtContext stm) {
    if (stm.opt_column_list() == null) {
      return Collections.emptyList();
    }
    return stm.opt_column_list().columnlist() == null ? Collections.emptyList() : stm
        .opt_column_list()
        .columnlist()
        .columnElem()
        .stream()
        .map(RuleContext::getText)
        .collect(Collectors.toList());
  }

  static String clearStr(ParseTree o, CharSequence c) {
    return StringUtils.removeFromStartEnd(o.getText(), c);
  }

  static String clearStr(ParseTree o, CharSequence c, CharSequence e) {
    return StringUtils.removeFromStartEnd(o.getText(), c, e);
  }

}
