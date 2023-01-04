grammar Csv2JdbcParser;

// CSV2J COPY :tableName FROM :csvPath WITH DELIMITER ';' CSV CREATE_TABLE

//options {
//    tokenVocab = Csv2JdbcLexer;
//    superClass = Csv2JdbcParserBase;
//}

@header {
  package com.mageddo.csv2jdc.antlr;
}


csv2j
  : stmt EOF
  ;

stmt
   : copystmt
   ;

copystmt
   : COPY qualified_name opt_column_list copy_from copy_file_name opt_with copy_options
   ;

copy_options
   : copy_opt_list
   ;

copy_opt_list
   : copy_opt_item*
   ;

copy_opt_item
   : DELIMITER sconst
   | CSV
   | HEADER
//   : BINARY
//   | FREEZE
//   | NULL_P sconst
//   | QUOTE sconst
//   | ESCAPE sconst
//   | FORCE QUOTE columnlist
//   | FORCE QUOTE STAR
//   | FORCE NOT NULL_P columnlist
//   | FORCE NULL_P columnlist
//   | ENCODING sconst
   ;

opt_column_list
   : OPEN_PAREN columnlist CLOSE_PAREN
   |
   ;

columnlist
   : columnElem (COMMA columnElem)*
   ;

columnElem
   : qualified_name
   ;

copy_from
   : FROM
   | TO
   ;

copy_file_name
   : qualified_name
   ;

qualified_name
  : StringConstant
  ;

sconst
    : StringConstant
    |
    ;

opt_with
   : WITH
   ;

COPY
   : 'CSV2JCOPY'
   ;

FROM
  : 'FROM'
  ;

TO
  : 'TO'
  ;

StringConstant
  : [A-Z]+
  ;

OPEN_PAREN
   : '('
   ;

CLOSE_PAREN
   : ')'
   ;

EscapeString
  : '\''  ~('\'')* '\''
  ;

DELIMITER
  : 'DELMITER'
  ;

CSV
  : 'CSV'
  ;

HEADER
  : 'HEADER'
  ;

COMMA
  : 'COMMA'
  ;

WITH
  : 'WITH'
  ;
