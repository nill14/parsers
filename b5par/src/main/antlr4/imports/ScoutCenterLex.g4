lexer grammar ScoutCenterLex;


FightersOnly : 'iba stíhače';
RunningFight : 'vojnový stav';
SectorName : Word 
           | Word WS Count
           | Word WS Word
           | Word WS Word WS Word;      

Ship : Word 
	 | Word WS Word;

PartyName : Word
		  | Word WS Word;




Relation : [VAN\-];
Header : 'Sektor' WS 'Vzťah' WS 'Štát' WS 'Lode' WS;


Word : CHAR+;

fragment CHAR : ~[ \t\r\n\[\]/:];

WS : [ \t\r\n]+ ; 

mode SECTOR_SHORT_MODE;
SectorShort : CHAR CHAR CHAR? -> mode(DEFAULT_MODE);

mode SHIP_CLASS_MODE;
ShipClass : [SKLD] -> mode(DEFAULT_MODE);

mode COUNT_MODE;
Count : [1-9][0-9]* -> mode(DEFAULT_MODE);
