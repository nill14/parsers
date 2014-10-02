// Define a grammar called Hello
grammar Hello;

r : name aLISTMEMBER (',' m)*;
//r  : 'hello' pair+ EOF;         // match keyword hello followed by an identifier


WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

LISTNAME: [a-z]+;
aLISTMEMBER: '[' a ']';
a : A;
A : [1-9]+;
//ANTLR automatically creates token from the ',' comma
name : LISTNAME;
m: aLISTMEMBER;   

