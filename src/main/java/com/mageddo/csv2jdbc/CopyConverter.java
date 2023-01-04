package com.mageddo.csv2jdbc;

import com.mageddo.csv2jdc.antlr.CopyLexer;
import com.mageddo.csv2jdc.antlr.CopyParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class CopyConverter {
  public static Csv2 of(String sql){
    return new CopyParser(new CommonTokenStream(new CopyLexer(CharStreams.fromString(sql))));
  }

  public static void main(String[] args) {
    final CopyParser parser = of("CSV2JCOPY TTORECORD (A, C, D)");

    System.out.println(parser.csv2j().table().tableName().getText());
//    System.out.println(parser.csv2j().table().columnName().get(0).getText());
//    System.out.println(parser.csv2j().table().tableName().getText());

    ParseTreeWalker.DEFAULT.walk(new com.mageddo.csv2jdc.antlr.CopyBaseListener() {

      @Override
      public void enterTable(CopyParser.TableContext ctx) {
        System.out.println(ctx.tableName().getText());
      }

      @Override
      public void enterTableName(CopyParser.TableNameContext ctx) {
        System.out.println(ctx.getText());
      }

      @Override
      public void enterPath(CopyParser.PathContext ctx) {
        System.out.println(ctx.getText());
      }
    }, parser.csv2j());

  }
}
