grammar Csv2Jdbc;

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

copystmt
   : COPY qualified_name opt_column_list copy_from copy_file_name opt_with copy_options
//   | COPY OPEN_PAREN preparablestmt CLOSE_PAREN TO copy_file_name opt_with copy_options
   | COPY preparablestmt TO copy_file_name opt_with copy_options
   ;

stmt
   : copystmt
   ;

preparablestmt
//  : selectstmt
//   : OPEN_PAREN AnyStatement CLOSE_PAREN
   : AnyStatement
  ;

selectstmt
   : anyStmt
   ;

anyStmt
  : AnyStatement
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
   : sconst
   ;

qualified_name
  : StringConstant
  ;

sconst
    : StringConstant
    | EscapeString
    ;

opt_with
   : WITH
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
   | CREATE_TABLE
   | ENCODING sconst
   ;

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

//fragment AnyChar
//  : [\u0000-\uFFFE]+
//  ;

CSV
  : 'CSV'
  ;

HEADER
  : 'HEADER'
  ;

DELIMITER
  : 'DELIMITER'
  ;

CREATE_TABLE
  : 'CREATE_TABLE'
  ;

ENCODING
    : 'ENCODING'
    ;

WITH
  : 'WITH'
  ;

COPY
   : 'CSV2J COPY'
   ;

FROM
  : 'FROM'
  ;

TO
  : 'TO'
  ;


OPEN_PAREN
   : '('
   ;

CLOSE_PAREN
   : ')'
   ;

COMMA
  : ','
  ;

AnyStatement
//  : (AnyChar | WS)+
  : '(' ~(')')+ ')'
//  : StringConstant
//  | WHITESPACE
  ;

EscapeString
  : '\''  ~('\'')* '\''
  ;



StringConstant
  : [A-Za-z_]+
  ;


fragment AnyChar
  : .
//  : [\u0000-\u00FF]
  ;


WS
   :  [ \t\n\r] + -> skip
   ;

//WHITESPACE
//  :
//  ;



