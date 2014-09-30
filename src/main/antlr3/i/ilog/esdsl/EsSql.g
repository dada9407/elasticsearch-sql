grammar	EsSql;
options{
 backtrack=true;
}

@header{
 package i.ilog.esdsl;
 import i.ilog.esdsl.Column;
 import i.ilog.esdsl.Term;
 import i.ilog.esdsl.Function;
 import i.ilog.esdsl.TableSource;
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import i.ilog.esdsl.WhereExp;
 import i.ilog.esdsl.OrExp;
 import i.ilog.esdsl.AndExp;
 import i.ilog.esdsl.InExp;
 import i.ilog.esdsl.NotExp;
 import i.ilog.esdsl.NotInExp;
 import i.ilog.esdsl.ExistExp;
 import i.ilog.esdsl.OperExp;
 import i.ilog.esdsl.BooleanExp;
 import i.ilog.esdsl.TopHitsFacet;
 import org.antlr.runtime.IntStream;
 import org.antlr.runtime.BitSet;
 import org.antlr.runtime.MismatchedTokenException;
 import org.antlr.runtime.RecognitionException;

}

@lexer::header{
 package i.ilog.esdsl;
}

@members {
    @Override
    protected Object recoverFromMismatchedToken(IntStream input, int ttype, BitSet follow) throws RecognitionException {
        throw new MismatchedTokenException(ttype, input);
    }
 
    @Override
    public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
        throw e;
    }
}
 
// Alter code generation so catch-clauses get replace with this action.
@rulecatch {
    catch (RecognitionException ea1) {
        throw ea1;
    }
}
 
@lexer::members {
    @Override
    public void reportError(RecognitionException ea2) {
        throw new RuntimeException(ea2);
    }
}

selectStatement returns [EsQueryBuilder builder = new EsQueryBuilder()]
	:	selectClause[builder] fromClause[builder] whereClause[builder]? limitClause[builder]? groupClause[builder]? facetClause[builder]* orderClause[builder]? /*{System.out.println(builder.toJson());}*/;
  
selectClause[EsQueryBuilder builder]
	:	SELECT cl=columList {builder.setSelectList(cl);};
	
fromClause[EsQueryBuilder builder]
	:	FROM ts=tableSource {builder.getTableSource().add(ts);} (COMMA ts1=tableSource {builder.getTableSource().add(ts1);})* ;


whereClause[EsQueryBuilder builder]
	:	WHERE exp=whereExp {builder.setFilter(exp);};
	
whereExp returns [WhereExp exp = new WhereExp()]
	@init{
		List e2= new ArrayList();
	}
	:	e1=andExp{} (OR e=andExp{e2.add(e); } )* {
		if(e2.size()>0){
			OrExp orexp = new OrExp();
			orexp.addExp(e1);
			orexp.addAllExp(e2);
			exp.setExp(orexp);
		}else{
			exp.setExp(e1);
		}
	};


andExp returns [AndExp andExp = new AndExp()]
	:	b1=booleanExp (AND b=booleanExp{andExp.addExp(b);})* {
		andExp.addExp(b1);
	};

	
booleanExp returns [BooleanExp booExp]
	:l=whereItem  oper=whereOper r=whereItem {
		booExp = new OperExp(l,$oper.text,r);
	}
	|ee=existExp {
		booExp = ee;
	}
	/*| ie=inExp {
		booExp = ie;
	}
	| nie=notInExp {
		booExp = nie;
	}*/
	| n=notExp {
		booExp = n;
	}
	| '(' we=whereExp ')' {
		booExp = we;
	};

whereOper
	:	NOT? WHERE_OPER;
	
whereItem returns [Object obj]
	:
	s=STRING {$obj=$s.text;}
	|t=TERM {$obj=new Term($t.text);}
	|d=INT {$obj=Integer.valueOf($d.text);}
	|'(' vl=inValList{$obj=vl;} ')' 
        | '(' v2=selectStatement{$obj=v2;} ')'
	| c=columnName{$obj=c;};
	
notExp returns [NotExp notExp = new NotExp()]
	:NOT  b=booleanExp {
	    notExp.addExp(b);
	};

existExp returns [ExistExp existExp= new ExistExp()] 
	:	EXIST '(' selectStatement')';
	
/*inExp	returns [InExp inExp = new InExp()]
	:	l=columnName  IN '(' r=inValList ')' {
		inExp.setColumn(l);
		inExp.setValueList(r);
		
	};
	
notInExp	returns [NotInExp notInExp = new NotInExp()]
	:	l=columnName NOT IN '(' r=inValList ')' {
		notInExp.setColumn(l);
		notInExp.setValueList(r);
		
	};
*/

inValList returns [List inList = new ArrayList()]
	:(s=STRING {inList.add($s.text);}
		| d=INT { inList.add(Integer.parseInt($d.text)); } | t=TERM{inList.add(new Term($t.text));})
	(COMMA ( s1=STRING {inList.add($s1.text);}
		|d1=DIGITAL+ { inList.add(Integer.parseInt($d1.text)); }))*;

	
groupClause [EsQueryBuilder builder]
	:	GROUP BY cl=columList {builder.setGroupList(cl);} (havingClause)?;
	
havingClause
	:	HAVING whereExp;
	
orderClause[EsQueryBuilder builder]
	:	ORDER BY  cn=columnName sort=(ASC|DESC)? { cn.setSortType($sort.text); builder.getSortList().add(cn); } 
		(COMMA cn1=columnName sort1=(ASC|DESC)?  { cn1.setSortType($sort1.text); builder.getSortList().add(cn1); })*;


columList returns [List<Column> cl=new ArrayList()]
	:	columItem[cl] (COMMA columItem[cl])*;
  
columItem[List cl]
	:	cn=columnName (AS iden=Identifier)? {
	    cn.setAlias($iden.text);
	    cl.add(cn);
	};

 
functionExp returns [Function fun= new Function()]
	: 	name=Identifier '(' (pl=paramList)? ')' {
		fun.setFunName($name.text);
		fun.setParamList(pl);
	};
	
paramList returns [List pl = new ArrayList()]
	: 	paramItem[pl] (COMMA paramItem[pl])*;
	
paramItem[List pl]
	: p=STRING {pl.add($p.text);} 
	| digi=INT {pl.add(Integer.parseInt($digi.text));}
	| cn=columnName {pl.add(cn);};

   
tableSource returns [TableSource ts = new TableSource()]
	:	(idx=Identifier {ts.setIndex($idx.text);} | idxtm=TERM {ts.setIndex($idxtm.text);}) ('.' (type=Identifier {ts.setType($type.text);} | typetm=TERM {ts.setIndex($typetm.text);}) )? (AS alias=Identifier {ts.setAlias($alias.text);})? | all=ALLCOLUMN {{ts.setIndex($all.text);}};
  
columnName returns [Column cln = new Column()]
	:	(type=Identifier {cln.setSourceType($type.text);} '.')? cn=Identifier {cln.setName($cn.text); cln.setType("normal");} 
	| ALLCOLUMN {cln.setName("*"); cln.setType("all");} 
	| fun=functionExp {cln.setFunction(fun);cln.setName(fun.getFunName());cln.setType("function");};
	
limitClause[EsQueryBuilder builder]
	:	LIMIT f=from COMMA s=size {
		builder.setFrom(Integer.parseInt($f.text));
		builder.setSize(Integer.parseInt($s.text));
	};
	
from
	:	INT;
size	:	INT;

facetClause[EsQueryBuilder builder]
	:	facet=dateHis{builder.getFacets().add(facet);}
        |       th=topHits{builder.getFacets().add(th);};
dateHis	returns [DateHisFacet facet = new DateHisFacet()]
	:	DATA_HIS c=columnName inte=Interval {facet.setField(c);facet.setInterval($inte.text);};


topHits returns [TopHitsFacet facet = new TopHitsFacet()]
        :       TOP_HITS num=INT{facet.setSize(Integer.parseInt($num.text));} sortSection[facet] includeSection[facet] ;
includeSection[TopHitsFacet facet]
        :       INCLUDE (f=columnName {facet.getFields().add(f);} (COMMA f1=columnName {facet.getFields().add(f1);})*) ;
sortSection[TopHitsFacet facet] 
        :       SORT BY   c=columnName sort=(DESC|ASC) {c.setSortType($sort.text);facet.getSortList().add(c);} (COMMA c1=columnName sort1=(DESC|ASC) {c1.setSortType($sort1.text);facet.getSortList().add(c1);})* ;
	
//Lexer

TOP_HITS
        :      ('T'|'t') ('O'|'o') ('P'|'p') '_' ('H'|'h') ('I'|'i') ('T'|'t') ('S'|'s');
 
SORT    :       ('S'|'s') ('O'|'o') ('R'|'r') ('T'|'t');

INCLUDE :       ('I'|'i') ('N'|'n') ('C'|'c') ('L'|'l') ('U'|'u') ('D'|'d') ('E'|'e');

COMMA	: 	',';

ALLCOLUMN
	:	'*';
	
INT	:	DIGITAL+ ;

fragment
LETTER	:	'a'..'z' | 'A'..'Z';

fragment
DIGITAL	:	'0'..'9';


fragment
UNIT	:	'Y'|'y'|'M'|'m'|'D'|'d'|'H'|'h'|'S'|'s'|'W'|'w'|'q'|'Q';

SELECT	:	('S'|'s') ('E'|'e') ('L'|'l') ('E'|'e') ('C'|'c') ('T'|'t');

ALL	:	('A'|'a') ('L'|'l') ('L'|'l') ;

DISTINCT:	('D'|'d') ('I'|'i') ('S'|'s') ('T'|'t') ('I'|'i') ('N'|'n') ('C'|'c') ('T'|'t');
  
TOP	:	('T'|'t') ('O'|'o') ('P'|'p');
  
FROM	:	('F'|'f') ('R'|'r') ('O'|'o') ('M'|'m');
  
WHERE	:	('W'|'w') ('H'|'h') ('E'|'e') ('R'|'r') ('E'|'e');
  
WHERE_OPER
	:	'>'|'<'|'='|'>='|'<='|'!='|'<>'|':'|LIKE|IN;
	
fragment
LIKE	:	('L'|'l') ('I'|'i') ('K'|'k') ('E'|'e');
  
OR	:	(('O'|'o') ('R'|'r')) | '||';

AND	:	(('A'|'a') ('N'|'n') ('D'|'d')) | '&&';

NOT	:	(('N'|'n') ('O'|'o') ('T'|'t')) | '!';

EXIST	:	('E'|'e') ('X'|'x') ('S'|'s') ('T'|'t');

fragment
IN	:	('I'|'i') ('n'|'N');

STRING	
	:	'\'' (options{greedy=false;} : .) * '\'' {setText(getText().substring(1,getText().length()-1));};

TERM	:	'\"' (options{greedy=false;} : .) * '\"' {setText(getText().substring(1,getText().length()-1));};

GROUP 	:	('G'|'g') ('R'|'r') ('O'|'o') ('U'|'u') ('P'|'p');

ORDER	:	('O'|'o') ('R'|'r') ('D'|'d') ('E'|'e') ('R'|'r');

LIMIT	:	('L'|'l') ('I'|'i') ('M'|'m') ('I'|'i') ('T'|'t');


BY	:	('B'|'b') ('Y'|'y');

ASC	:	('A'|'a') ('S'|'s') ('C'|'c');

DESC	:	('D'|'d') ('E'|'e') ('S'|'s') ('C'|'c');

HAVING	:	('H'|'h') ('A'|'a') ('V'|'v') ('I'|'i') ('N'|'n') ('G'|'g');

AS	:	('A'|'a') ('S'|'s');

DATA_HIS:	('d'|'D') ('a'|'A') ('t'|'T') ('e'|'E') '_' ('h'|'H') ('i'|'I') ('s'|'S');

Interval:	DIGITAL+ UNIT;

Identifier
	:	 (LETTER | '_' | '-' | '@' )  (LETTER | '_' | '-' | '@' | DIGITAL)*;	

WS	:	(' ' | '\t' | '\n' | '\r')+  {$channel=HIDDEN;};
