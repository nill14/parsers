grammar ScoutCenter;

content : header? sectorContent (WS sectorContent)*? EOF;

header  : Header;

sectorContent : sector WS (runningFight WS)? party (WS party)*;

sector : sectorName WS '['sectorShort']';

party : relations WS partyName WS (fightersOnly | fleetItem (WS fleetItem)*);


relations : relation '/' relation;


fleetItem : '['shipClass']' WS ship ':' WS count;


relation: Relation;
count: Count;
sectorShort : CHAR CHAR CHAR?;
shipClass : ShipClass;

fightersOnly : FightersOnly;
runningFight : RunningFight;
sectorName : SectorName;
ship : Ship;
partyName : PartyName;





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



ShipClass : [SKLD];

Count : [1-9][0-9]*;

