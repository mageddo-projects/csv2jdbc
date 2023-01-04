grammar Copy;

// CSV2J COPY :tableName FROM :csvPath WITH DELIMITER ';' CSV CREATE_TABLE

@header {
  package com.mageddo.csv2jdc.antlr;
}

csv2j
  : STATEMENT_START tableName ( '(' (columnName)+ ')' ) FROM path options;

options: WITH (option)+;

option: optionName (optionValue)?;

optionValue
  : STRING
  ;

path
  : STRING
  ;

columnName : STATEMENT_NAME;

tableName : STATEMENT_NAME;

optionName : STATEMENT_NAME;

fragment HEX
  : [0-9a-fA-F]
  ;

fragment SAFECODEPOINT
   : ~ ['"\\\u0000-\u001F]
   ;

fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;

fragment STATEMENT_NAME_PATTERN: [a-zA-Z_]+ ;

fragment ESC
   : '\\' (['"\\/bfnrt] | UNICODE)
   ;

TABLE_NAME
  : STATEMENT_NAME_PATTERN
  ;

STATEMENT_NAME
  : STATEMENT_NAME_PATTERN
  ;

STATEMENT_START: 'CSV2J COPY';

WITH: 'WITH';

FROM: 'FROM';

STRING
   : '"' (ESC | SAFECODEPOINT)* '"'
   | '\'' (ESC | SAFECODEPOINT)* '\''
   ;

WS
   : [ \t\n\r] + -> skip
   ;
