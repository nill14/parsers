lexer grammar XiLexer;

STRING : '\"' .*? '\"';
BOOL : 'bool';
INT : 'int';
RETURN : 'return';
WHILE : 'while';
IF : 'if';
ELSE : 'else';
LENGTH : 'length';
BREAK : 'break';
TRUE : 'true';
FALSE : 'false';
ID : [a-zA-Z] [a-zA-Z_0-9\']*;
INTEGER : [0-9]+;

LBRACKET : '[';
RBRACKET : ']';
COLON : ':';
EQUAL : '=';
LPAREN : '(';
RPAREN : ')';
COMMA : ',';
SEMICOLON : ';';
LBRACE : '{';
RBRACE : '}';
STAR : '*';
SLASH : '/';
PERCENT : '%';
PLUS : '+';
DASH : '-';
LANGLE : '<';
RANGLE : '>';
BANG : '!';
AMPERSAND : '&';
PIPE : '|';

WS : [ \t\r\n]+ -> skip;
COMMENT : '//' .*? [\r\n] -> skip;