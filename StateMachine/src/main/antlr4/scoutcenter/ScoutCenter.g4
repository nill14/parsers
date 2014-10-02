grammar ScoutCenter;


content : header? sectorContent (WS sectorContent)*? EOF;

header : Header;

sectorContent : sector WS (fight WS)? party (WS party)*;

sector : sectorName WS sectorShort;
sectorName : Word 
           | Word WS Number
           | Word WS Word
           | Word WS Word WS Word;
sectorShort : SectorShort ;

party : relations WS partyName WS (fighters | fleet (WS fleet)* );

relations : relation '/' relation;

relation : Relation;
partyName : Word (WS Word)?;

fleet : shipClass WS ship ':' WS count;

shipClass : ShipClass;
ship : Word (WS Word)?;
count : Number;  

ShipClass : '[' [SKLD] ']';

SectorShort : '[' CH CH CH? ']';
Number : NUM+;
Relation : [VAN\\-];
fighters : 'iba stíhače';
fight : 'vojnový stav';
Word : CH+;

Header : 'Sektor' WS 'Vzťah' WS 'Štát' WS 'Lode' WS;

fragment NUM    :  [0-9];
fragment CH : [A-Za-z0-9] 
            | [ěščřžýáíéóťŠČŘŽÝÁÍÉŮÓ] 
            | '\'';


WS : [ \t\r\n]+ /*-> skip*/ ; 