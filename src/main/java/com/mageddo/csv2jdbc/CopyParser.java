//package com.mageddo.csv2jdbc;
//
//
//import org.antlr.v4.runtime.BaseErrorListener;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.antlr.v4.runtime.RecognitionException;
//import org.antlr.v4.runtime.Recognizer;
//import org.antlr.v4.runtime.TokenStreamRewriter;
//import org.antlr.v4.runtime.tree.ParseTreeWalker;
//
//public class CopyParser {
//  public String walk(String gradle, CopyRewriterListener rewriterListener){
//    final var lexer = new GradleLexer(CharStreams.fromString(gradle));
//    lexer.addErrorListener(new BaseErrorListener(){
//      public void syntaxError(
//          Recognizer<?, ?> recognizer,
//          Object offendingSymbol,
//          int line,
//          int charPositionInLine,
//          String msg,
//          RecognitionException e
//      ) {
//        throw new IllegalArgumentException(String.format(
//            "line %d:%d %s",
//            line,
//            charPositionInLine,
//            msg
//        ), e);
//      }
//    });
//    final var tokens = new CommonTokenStream(lexer);
//    final var parser = new GradleParser(tokens);
//    final var rewriter = new TokenStreamRewriter(tokens);
//    rewriterListener.setRewriter(rewriter);
//    ParseTreeWalker.DEFAULT.walk(rewriterListener, parser.gradle());
//    return rewriter.getText();
//  }
//}
