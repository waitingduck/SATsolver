import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import javax.sql.rowset.Predicate;






public class SATSolver {
	public static void main(String[] args)
	{
		///////////////////////////////////////////////file IO/////////////////////////////////////////////////////////////////
		Queue<Integer> q=new LinkedList<Integer>();// store the read numbers
		int tempread=0;//temporary record the read number
		Scanner inputStream = null;
		
		try
		{
			inputStream = new Scanner(new FileInputStream(args[0]));
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File was not found!");
		}
		PrintWriter outputresult = null;
			try
			{
				outputresult = new PrintWriter(new FileOutputStream(args[1]));
			}
			catch(FileNotFoundException e)
			{
				System.out.println("Error opening file the output");
				System.exit(0);
			}
			
		
		while(inputStream.hasNextInt())
		{
			tempread = inputStream.nextInt();
			q.add(tempread);
		}
		
		inputStream.close();//end of fileIO
		
///////////////////////////////////////////////store numbers/////////////////////////////////////////////////////////////////
		int chemical=0,container=0,containerLimit=0;//initialize chemical and container
		chemical=q.poll();
		container=q.poll();
		int[][] arr = new int[chemical][chemical];//array to store yes/no list
		
		System.out.println("There are " + chemical +" kind of chimical and " + container+ " containers with " + containerLimit +" containerLimit");
		
		for(int i = 0;i<chemical;i++)
		{
			for(int j =0;j<chemical;j++)
			{
				arr[i][j]=q.poll();
				System.out.print(arr[i][j] + " ");
			}
			System.out.println("");
		}		
///////////////////////////////////////////////transform to CNF and add to KB/////////////////////////////////////////////////////////////////	
		KnowledgeBase kb = new KnowledgeBase();//i=container j=chemical
		String tempcnf="";
		
		for(int chem= 0;chem<chemical;chem++)//one chemical must contained in only one container
		{
			for(int con1=0;con1<container-1;con1++)
			{
				for(int con2=con1+1;con2<container;con2++)
				{

					tempcnf="((NOT X" + chem+con1 +") OR (NOT X" + chem+con2 + "))";
					System.out.println(tempcnf);
					kb.tell(tempcnf);
				}
				System.out.println("");
			}
		}
		System.out.println("");
		
		for(int chem= 0;chem<chemical;chem++)//one chemical must contained in only one container
		{
			tempcnf="";
			for(int con=0;con<container;con++)
			{
				if(tempcnf!="")
				{
					tempcnf= "("+ tempcnf+" OR X"+chem+con + ")";
				}
				else
				{
					tempcnf="(X"+chem+con;
				}
			}
			tempcnf=tempcnf+")";
			System.out.println(tempcnf);
			kb.tell(tempcnf);
			System.out.println("");
		}
		for(int chem1=0;chem1<chemical-1;chem1++)//chemical in no list must not contain in the same container
		{
			for(int chem2=chem1+1;chem2<chemical;chem2++)
			{
					if(arr[chem1][chem2]==-1)
					{
						for(int con=0;con<container;con++)
						{
							tempcnf="((NOT X" + chem1+con + ") OR (NOT X" +chem2+con + "))";
							System.out.println(tempcnf);
							kb.tell(tempcnf);
						}
					}
			}
		}
		System.out.println("");
		for(int chem1=0;chem1<chemical-1;chem1++)//chemical in yes list must contain in the same container
		{
			for(int chem2=chem1+1;chem2<chemical;chem2++)
			{
					if(arr[chem1][chem2]==1)
					{
							for(int con=0;con<container;con++)
							{
								tempcnf="((NOT X" + chem1+con + ") OR X" + chem2+con + ") AND (X" + chem1+con + " OR (NOT X" + chem2+con + "))";
								//System.out.println(tempcnf);
								kb.tell(tempcnf);
							}
					}
			}
		}
		
///////////////////////////////////////////////PL Resolution/////////////////////////////////////////////////////////////////	
		SATSolver pl = new SATSolver();
		SATSolver.PLResolution p = new PLResolution();
		SATSolver.WalkSAT w = new WalkSAT();
		Model m = new Model();
		double fp=Double.parseDouble(args[2]);
		int max_flip=Integer.parseInt(args[3]);
		if(p.plResolution(kb,"XX")==true)
		{
			System.out.println("it has a solution");
			outputresult.println("1");
		}
		else
		{
			System.out.println("it has no solution");
			outputresult.println("0");
		}
		m=w.findModelFor(kb.toString(),max_flip,fp,chemical,container);
		if(m!=null)
		{
			System.out.println("1");
			m.print(container,chemical,outputresult);
			/*for(int con=0;con<container;con++)
			{
				for(int chem=0;chem<chemical;chem++)
				{
					if(m.isTrue("X"+chem+con))
					{
						outputresult.print("1 ");
					}
					else
					{
						outputresult.print("0 ");
					}
				}
				outputresult.println("");
			}*/
		}
		else
		{
			System.out.println("0");
		}
		outputresult.close();
	}
	////////////////////////////end of program/////////////////////////////////
	public static class TTEntails {
		public boolean ttEntails(KnowledgeBase kb, String alpha) {
			Sentence kbSentence = kb.asSentence();
			Sentence querySentence = (Sentence) new PEParser().parse(alpha);
			SymbolCollector collector = new SymbolCollector();
			Set<Symbol> kbSymbols = collector.getSymbolsIn(kbSentence);
			Set<Symbol> querySymbols = collector.getSymbolsIn(querySentence);
			Set<Symbol> symbols = new SetOps<Symbol>().union(kbSymbols,
					querySymbols);
			List<Symbol> symbolList = new Converter<Symbol>()
					.setToList(symbols);
			return ttCheckAll(kbSentence, querySentence, symbolList,
					new Model());
		}

		public boolean ttCheckAll(Sentence kbSentence, Sentence querySentence,
				List symbols, Model model) {
			if (symbols.isEmpty()) {
				if (model.isTrue(kbSentence)) {
					// System.out.println("#");
					return model.isTrue(querySentence);
				} else {
					// System.out.println("0");
					return true;
				}
			} else {
				Symbol symbol = (Symbol) Util.first(symbols);
				List rest = Util.rest(symbols);

				Model trueModel = model.extend(new Symbol(symbol.getValue()),
						true);
				Model falseModel = model.extend(new Symbol(symbol.getValue()),
						false);
				return (ttCheckAll(kbSentence, querySentence, rest, trueModel) && (ttCheckAll(
						kbSentence, querySentence, rest, falseModel)));
			}
		}
	}

	public static class PELexer extends Lexer {

		Set<String> connectors;

		public PELexer() {
			connectors = new HashSet<String>();
			connectors.add("NOT");
			connectors.add("AND");
			connectors.add("OR");
			connectors.add("=>");
			connectors.add("<=>");
		}

		public PELexer(String inputString) {
			this();
			setInput(inputString);
		}

		@Override
		public Token nextToken() {
			Token result = null;
			int tokenType;
			String tokenContent;

			if (lookAhead(1) == '(') {
				consume();
				return new Token(LogicTokenTypes.LPAREN, "(");

			} else if (lookAhead(1) == ')') {
				consume();
				return new Token(LogicTokenTypes.RPAREN, ")");
			} else if (identifierDetected()) {
				return symbol();

			} else if (Character.isWhitespace(lookAhead(1))) {
				consume();
				return nextToken();
				// return whiteSpace();
			} else if (lookAhead(1) == (char) -1) {
				return new Token(LogicTokenTypes.EOI, "EOI");
			} else {
				throw new RuntimeException("Lexing error on character "
						+ lookAhead(1));
			}

		}

		private boolean identifierDetected() {
			return (Character.isJavaIdentifierStart((char) lookAheadBuffer[0]))
					|| partOfConnector();
		}

		private boolean partOfConnector() {
			return (lookAhead(1) == '=') || (lookAhead(1) == '<')
					|| (lookAhead(1) == '>');
		}

		private Token symbol() {
			StringBuffer sbuf = new StringBuffer();
			while ((Character.isLetterOrDigit(lookAhead(1)))
					|| (lookAhead(1) == '=') || (lookAhead(1) == '<')
					|| (lookAhead(1) == '>')) {
				sbuf.append(lookAhead(1));
				consume();
			}
			String symbol = sbuf.toString();
			if (isConnector(symbol)) {
				return new Token(LogicTokenTypes.CONNECTOR, sbuf.toString());
			} else if (symbol.equalsIgnoreCase("true")) {
				return new Token(LogicTokenTypes.TRUE, "TRUE");
			} else if (symbol.equalsIgnoreCase("false")) {
				return new Token(LogicTokenTypes.FALSE, "FALSE");
			} else {
				return new Token(LogicTokenTypes.SYMBOL, sbuf.toString());
			}

		}

		private Token connector() {
			StringBuffer sbuf = new StringBuffer();
			while (Character.isLetterOrDigit(lookAhead(1))) {
				sbuf.append(lookAhead(1));
				consume();
			}
			return new Token(LogicTokenTypes.CONNECTOR, sbuf.toString());
		}

		private Token whiteSpace() {
			StringBuffer sbuf = new StringBuffer();
			while (Character.isWhitespace(lookAhead(1))) {
				sbuf.append(lookAhead(1));
				consume();
			}
			return new Token(LogicTokenTypes.WHITESPACE, sbuf.toString());

		}

		private boolean isConnector(String aSymbol) {
			return (connectors.contains(aSymbol));
		}

	}


	public static class PEParser extends Parser {

		public PEParser() {
			lookAheadBuffer = new Token[lookAhead];
		}

		@Override
		public ParseTreeNode parse(String inputString) {
			lexer = new PELexer(inputString);
			fillLookAheadBuffer();
			return parseSentence();
		}

		private TrueSentence parseTrue() {

			consume();
			return new TrueSentence();

		}

		private FalseSentence parseFalse() {
			consume();
			return new FalseSentence();

		}

		private Symbol parseSymbol() {
			String sym = lookAhead(1).getText();
			consume();
			return new Symbol(sym);
		}

		private AtomicSentence parseAtomicSentence() {
			Token t = lookAhead(1);
			if (t.getType() == LogicTokenTypes.TRUE) {
				return parseTrue();
			} else if (t.getType() == LogicTokenTypes.FALSE) {
				return parseFalse();
			} else if (t.getType() == LogicTokenTypes.SYMBOL) {
				return parseSymbol();
			} else {
				throw new RuntimeException(
						"Error in parseAtomicSentence with Token "
								+ lookAhead(1));
			}
		}

		private UnarySentence parseNotSentence() {
			match("NOT");
			Sentence sen = parseSentence();
			return new UnarySentence(sen);
		}

		private MultiSentence parseMultiSentence() {
			consume();
			String connector = lookAhead(1).getText();
			consume();
			List<Sentence> sentences = new ArrayList<Sentence>();
			while (lookAhead(1).getType() != LogicTokenTypes.RPAREN) {
				Sentence sen = parseSentence();
				// consume();
				sentences.add(sen);
			}
			match(")");
			return new MultiSentence(connector, sentences);
		}

		private Sentence parseSentence() {
			if (detectAtomicSentence()) {
				return parseAtomicSentence();
			} else if (detectBracket()) {
				return parseBracketedSentence();
			} else if (detectNOT()) {
				return parseNotSentence();
			} else {

				throw new RuntimeException("Parser Error Token = "
						+ lookAhead(1));
			}
		}

		private boolean detectNOT() {
			return (lookAhead(1).getType() == LogicTokenTypes.CONNECTOR)
					&& (lookAhead(1).getText().equals("NOT"));
		}

		private Sentence parseBracketedSentence() {

			if (detectMultiOperator()) {
				return parseMultiSentence();
			} else {
				match("(");
				Sentence one = parseSentence();
				if (lookAhead(1).getType() == LogicTokenTypes.RPAREN) {
					match(")");
					return one;
				} else if ((lookAhead(1).getType() == LogicTokenTypes.CONNECTOR)
						&& (!(lookAhead(1).getText().equals("Not")))) {
					String connector = lookAhead(1).getText();
					consume(); // connector
					Sentence two = parseSentence();
					match(")");
					return new BinarySentence(connector, one, two);
				}

			}
			throw new RuntimeException(
					" Runtime Exception at Bracketed Expression with token "
							+ lookAhead(1));
		}

		private boolean detectMultiOperator() {
			return (lookAhead(1).getType() == LogicTokenTypes.LPAREN)
					&& ((lookAhead(2).getText().equals("AND")) || (lookAhead(2)
							.getText().equals("OR")));
		}

		private boolean detectBracket() {
			return lookAhead(1).getType() == LogicTokenTypes.LPAREN;
		}

		private boolean detectAtomicSentence() {
			int type = lookAhead(1).getType();
			return (type == LogicTokenTypes.TRUE)
					|| (type == LogicTokenTypes.FALSE)
					|| (type == LogicTokenTypes.SYMBOL);
		}
	}

	public static class MultiSentence extends ComplexSentence {
		private String operator;

		private List<Sentence> sentences;

		public MultiSentence(String operator, List<Sentence> sentences) {
			this.operator = operator;
			this.sentences = sentences;
		}

		public String getOperator() {
			return operator;
		}

		public List getSentences() {
			return sentences;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}
			if ((o == null) || (this.getClass() != o.getClass())) {
				return false;
			}
			MultiSentence sen = (MultiSentence) o;
			return ((sen.getOperator().equals(getOperator())) && (sen
					.getSentences().equals(getSentences())));

		}

		@Override
		public int hashCode() {
			int result = 17;
			for (Sentence s : sentences) {
				result = 37 * result + s.hashCode();
			}
			return result;
		}

		@Override
		public String toString() {
			String part1 = "( " + getOperator() + " ";
			for (int i = 0; i < getSentences().size(); i++) {
				part1 = part1 + sentences.get(i).toString() + " ";
			}
			return part1 + " ) ";
		}

		@Override
		public Object accept(PLVisitor plv, Object arg) {
			return plv.visitMultiSentence(this, arg);
		}

	}

	public static class FalseSentence extends AtomicSentence {
		@Override
		public String toString() {
			return "FALSE";
		}

		@Override
		public Object accept(PLVisitor plv, Object arg) {
			return plv.visitFalseSentence(this, arg);
		}
	}

	public static class AndDetector implements PLVisitor {

		public Object visitSymbol(Symbol s, Object arg) {

			return new Boolean(false);
		}

		public Object visitTrueSentence(TrueSentence ts, Object arg) {
			return new Boolean(false);
		}

		public Object visitFalseSentence(FalseSentence fs, Object arg) {
			return new Boolean(false);
		}

		public Object visitNotSentence(UnarySentence fs, Object arg) {
			return fs.getNegated().accept(this, null);
		}

		public Object visitBinarySentence(BinarySentence fs, Object arg) {
			if (fs.isAndSentence()) {
				return new Boolean(true);
			} else {
				boolean first = ((Boolean) fs.getFirst().accept(this, null))
						.booleanValue();
				boolean second = ((Boolean) fs.getSecond().accept(this, null))
						.booleanValue();
				return new Boolean((first || second));
			}
		}

		public Object visitMultiSentence(MultiSentence fs, Object arg) {
			throw new RuntimeException("can't handle multisentences");
		}

		public boolean containsEmbeddedAnd(Sentence s) {
			return ((Boolean) s.accept(this, null)).booleanValue();
		}

	}

	public static class CNFClauseGatherer extends BasicTraverser {
		AndDetector detector;

		public CNFClauseGatherer() {
			detector = new AndDetector();
		}

		@Override
		public Object visitBinarySentence(BinarySentence bs, Object args) {

			Set<Sentence> soFar = (Set<Sentence>) args;

			Sentence first = bs.getFirst();
			Sentence second = bs.getSecond();
			processSubTerm(second, processSubTerm(first, soFar));

			return soFar;

		}

		private Set<Sentence> processSubTerm(Sentence s, Set<Sentence> soFar) {
			if (detector.containsEmbeddedAnd(s)) {
				return (Set<Sentence>) s.accept(this, soFar);
			} else {
				soFar.add(s);
				return soFar;
			}
		}

		public Set<Sentence> getClausesFrom(Sentence sentence) {
			Set<Sentence> set = new HashSet<Sentence>();
			if (sentence instanceof Symbol) {
				set.add(sentence);
			} else if (sentence instanceof UnarySentence) {
				set.add(sentence);
			} else {
				set = (Set<Sentence>) sentence.accept(this, set);
			}
			return set;
		}

	}

	public static class SetOps<T> {
		public Set<T> union(Set<T> one, Set<T> two) {
			Set<T> union = new HashSet<T>(one);
			union.addAll(two);
			return union;
		}

		public Set<T> intersection(Set<T> one, Set<T> two) {
			Set<T> intersection = new HashSet<T>(one);
			intersection.retainAll(two);
			return intersection;
		}

		public Set<T> difference(Set<T> one, Set<T> two) {
			Set<T> three = new HashSet<T>();
			Iterator<T> iteratorOne = one.iterator();
			while (iteratorOne.hasNext()) {
				T sym = iteratorOne.next();
				if (!(in(two, sym))) {
					three.add(sym);
				}
			}
			return three;
		}

		public boolean in(Set<T> s, T o) {

			Iterator<T> i = s.iterator();
			while (i.hasNext()) {
				Object obj = i.next();
				if (obj.equals(o)) {
					return true;
				}
			}
			return false;
		}

	}

	public static class BasicTraverser implements PLVisitor {

		public Object visitSymbol(Symbol s, Object arg) {
			return arg;
		}

		public Object visitTrueSentence(TrueSentence ts, Object arg) {
			return arg;
		}

		public Object visitFalseSentence(FalseSentence fs, Object arg) {
			return arg;
		}

		public Object visitNotSentence(UnarySentence ns, Object arg) {
			Set s = (Set) arg;
			return new SetOps().union(s, (Set) ns.getNegated()
					.accept(this, arg));
		}

		public Object visitBinarySentence(BinarySentence bs, Object arg) {
			Set s = (Set) arg;
			Set termunion = new SetOps().union(
					(Set) bs.getFirst().accept(this, arg), (Set) bs.getSecond()
							.accept(this, arg));
			return new SetOps().union(s, termunion);
		}

		public Object visitMultiSentence(MultiSentence fs, Object arg) {
			throw new RuntimeException("Can't handle MultiSentence");
		}

	}

	public static class AbstractPLVisitor implements PLVisitor {
		private PEParser parser = new PEParser();

		public Object visitSymbol(Symbol s, Object arg) {
			return new Symbol(s.getValue());
		}

		public Object visitTrueSentence(TrueSentence ts, Object arg) {
			return new TrueSentence();
		}

		public Object visitFalseSentence(FalseSentence fs, Object arg) {
			return new FalseSentence();
		}

		public Object visitNotSentence(UnarySentence fs, Object arg) {
			return new UnarySentence((Sentence) fs.getNegated().accept(this,
					arg));
		}

		public Object visitBinarySentence(BinarySentence fs, Object arg) {
			return new BinarySentence(fs.getOperator(), (Sentence) fs
					.getFirst().accept(this, arg), (Sentence) fs.getSecond()
					.accept(this, arg));
		}

		public Object visitMultiSentence(MultiSentence fs, Object arg) {
			List terms = fs.getSentences();
			List<Sentence> newTerms = new ArrayList<Sentence>();
			for (int i = 0; i < terms.size(); i++) {
				Sentence s = (Sentence) terms.get(i);
				Sentence subsTerm = (Sentence) s.accept(this, arg);
				newTerms.add(subsTerm);
			}
			return new MultiSentence(fs.getOperator(), newTerms);
		}

		protected Sentence recreate(Object ast) {
			return (Sentence) parser.parse(((Sentence) ast).toString());
		}

	}

	public static class KnowledgeBase {
		private List<Sentence> sentences;

		private PEParser parser;

		public KnowledgeBase() {
			sentences = new ArrayList<Sentence>();
			parser = new PEParser();
		}

		public void tell(String aSentence) {
			Sentence sentence = (Sentence) parser.parse(aSentence);
			if (!(sentences.contains(sentence))) {
				sentences.add(sentence);
			}
		}

		public void tellAll(String[] percepts) {
			for (int i = 0; i < percepts.length; i++) {
				tell(percepts[i]);
			}

		}

		public int size() {
			return sentences.size();
		}

		public Sentence asSentence() {
			return LogicUtils.chainWith("AND", sentences);
		}

		public boolean askWithDpll(String queryString) {
			Sentence query = null, cnfForm = null;
			try {
				// just a check to see that the query is well formed
				query = (Sentence) parser.parse(queryString);
			} catch (Exception e) {
				System.out.println("error parsing query" + e.getMessage());
			}

			Sentence kbSentence = asSentence();
			Sentence kbPlusQuery = null;
			if (kbSentence != null) {
				kbPlusQuery = (Sentence) parser.parse(" ( "
						+ kbSentence.toString() + " AND " + queryString + " )");
			} else {
				kbPlusQuery = query;
			}
			try {
				cnfForm = new CNFTransformer().transform(kbPlusQuery);
				// System.out.println(cnfForm.toString());
			} catch (Exception e) {
				System.out.println("error converting kb +  query to CNF"
						+ e.getMessage());

			}
			return new DPLL().dpllSatisfiable(cnfForm);
		}

		public boolean askWithTTEntails(String queryString) {

			return new TTEntails().ttEntails(this, queryString);
		}

		@Override
		public String toString() {
			if (sentences.size() == 0) {
				return "";
			} else
				return asSentence().toString();
		}

		public List getSentences() {
			return sentences;
		}
	}

	public static class DPLL {

		private final Converter<Symbol> SYMBOL_CONVERTER = new Converter<Symbol>();

		public boolean dpllSatisfiable(Sentence s) {

			return dpllSatisfiable(s, new Model());
		}

		public boolean dpllSatisfiable(String string) {
			Sentence sen = (Sentence) new PEParser().parse(string);
			return dpllSatisfiable(sen, new Model());
		}

		public boolean dpllSatisfiable(Sentence s, Model m) {
			Set<Sentence> clauses = new CNFClauseGatherer()
					.getClausesFrom(new CNFTransformer().transform(s));
			List symbols = SYMBOL_CONVERTER.setToList(new SymbolCollector()
					.getSymbolsIn(s));
			// System.out.println(" numberOfSymbols = " + symbols.size());
			return dpll(clauses, symbols, m);
		}

		private boolean dpll(Set<Sentence> clauses, List symbols, Model model) {
			// List<Sentence> clauseList = asList(clauses);
			List<Sentence> clauseList = new Converter<Sentence>()
					.setToList(clauses);
			// System.out.println("clauses are " + clauses.toString());
			// if all clauses are true return true;
			if (areAllClausesTrue(model, clauseList)) {
				// System.out.println(model.toString());
				return true;
			}
			// if even one clause is false return false
			if (isEvenOneClauseFalse(model, clauseList)) {
				// System.out.println(model.toString());
				return false;
			}
			// System.out.println("At least one clause is unknown");
			// try to find a unit clause
			SymbolValuePair svp = findPureSymbolValuePair(clauseList, model,
					symbols);
			if (svp.notNull()) {
				List newSymbols = (List) ((ArrayList) symbols).clone();
				newSymbols.remove(new Symbol(svp.symbol.getValue()));
				Model newModel = model.extend(
						new Symbol(svp.symbol.getValue()),
						svp.value.booleanValue());
				return dpll(clauses, newSymbols, newModel);
			}

			SymbolValuePair svp2 = findUnitClause(clauseList, model, symbols);
			if (svp2.notNull()) {
				List newSymbols = (List) ((ArrayList) symbols).clone();
				newSymbols.remove(new Symbol(svp2.symbol.getValue()));
				Model newModel = model.extend(
						new Symbol(svp2.symbol.getValue()),
						svp2.value.booleanValue());
				return dpll(clauses, newSymbols, newModel);
			}

			Symbol symbol = (Symbol) symbols.get(0);
			// System.out.println("default behaviour selecting " + symbol);
			List newSymbols = (List) ((ArrayList) symbols).clone();
			newSymbols.remove(0);
			return (dpll(clauses, newSymbols, model.extend(symbol, true)) || dpll(
					clauses, newSymbols, model.extend(symbol, false)));
		}

		private boolean isEvenOneClauseFalse(Model model, List clauseList) {
			for (int i = 0; i < clauseList.size(); i++) {
				Sentence clause = (Sentence) clauseList.get(i);
				if (model.isFalse(clause)) {
					// System.out.println(clause.toString() + " is false");
					return true;
				}

			}

			return false;
		}

		private boolean areAllClausesTrue(Model model, List clauseList) {

			for (int i = 0; i < clauseList.size(); i++) {
				Sentence clause = (Sentence) clauseList.get(i);
				// System.out.println("evaluating " + clause.toString());
				if (!isClauseTrueInModel(clause, model)) { // ie if false or
					// UNKNOWN
					// System.out.println(clause.toString()+ " is not true");
					return false;
				}

			}
			return true;
		}

		private boolean isClauseTrueInModel(Sentence clause, Model model) {
			List<Symbol> positiveSymbols = SYMBOL_CONVERTER
					.setToList(new SymbolClassifier()
							.getPositiveSymbolsIn(clause));
			List<Symbol> negativeSymbols = SYMBOL_CONVERTER
					.setToList(new SymbolClassifier()
							.getNegativeSymbolsIn(clause));

			for (Symbol symbol : positiveSymbols) {
				if ((model.isTrue(symbol))) {
					return true;
				}
			}
			for (Symbol symbol : negativeSymbols) {
				if ((model.isFalse(symbol))) {
					return true;
				}
			}
			return false;

		}

		public List<Sentence> clausesWithNonTrueValues(
				List<Sentence> clauseList, Model model) {
			List<Sentence> clausesWithNonTrueValues = new ArrayList<Sentence>();
			for (int i = 0; i < clauseList.size(); i++) {
				Sentence clause = clauseList.get(i);
				if (!(isClauseTrueInModel(clause, model))) {
					if (!(clausesWithNonTrueValues.contains(clause))) {// defensive
						// programming not really necessary
						clausesWithNonTrueValues.add(clause);
					}
				}

			}
			return clausesWithNonTrueValues;
		}

		public SymbolValuePair findPureSymbolValuePair(
				List<Sentence> clauseList, Model model, List symbols) {
			List clausesWithNonTrueValues = clausesWithNonTrueValues(
					clauseList, model);
			Sentence nonTrueClauses = LogicUtils.chainWith("AND",
					clausesWithNonTrueValues);
			// System.out.println("Unsatisfied clauses = "
			// + clausesWithNonTrueValues.size());
			Set<Symbol> symbolsAlreadyAssigned = model.getAssignedSymbols();

			// debug
			// List symList = asList(symbolsAlreadyAssigned);
			//
			// System.out.println(" assignedSymbols = " + symList.size());
			// if (symList.size() == 52) {
			// System.out.println("untrue clauses = " +
			// clausesWithNonTrueValues);
			// System.out.println("model= " + model);
			// }

			// debug
			List<Symbol> purePositiveSymbols = SYMBOL_CONVERTER
					.setToList(new SetOps<Symbol>().difference(
							new SymbolClassifier()
									.getPurePositiveSymbolsIn(nonTrueClauses),
							symbolsAlreadyAssigned));

			List<Symbol> pureNegativeSymbols = SYMBOL_CONVERTER
					.setToList(new SetOps<Symbol>().difference(
							new SymbolClassifier()
									.getPureNegativeSymbolsIn(nonTrueClauses),
							symbolsAlreadyAssigned));
			// if none found return "not found
			if ((purePositiveSymbols.size() == 0)
					&& (pureNegativeSymbols.size() == 0)) {
				return new SymbolValuePair();// automatically set to null values
			} else {
				if (purePositiveSymbols.size() > 0) {
					Symbol symbol = new Symbol(
							(purePositiveSymbols.get(0)).getValue());
					if (pureNegativeSymbols.contains(symbol)) {
						throw new RuntimeException("Symbol "
								+ symbol.getValue() + "misclassified");
					}
					return new SymbolValuePair(symbol, true);
				} else {
					Symbol symbol = new Symbol(
							(pureNegativeSymbols.get(0)).getValue());
					if (purePositiveSymbols.contains(symbol)) {
						throw new RuntimeException("Symbol "
								+ symbol.getValue() + "misclassified");
					}
					return new SymbolValuePair(symbol, false);
				}
			}
		}

		private SymbolValuePair findUnitClause(List clauseList, Model model,
				List symbols) {
			for (int i = 0; i < clauseList.size(); i++) {
				Sentence clause = (Sentence) clauseList.get(i);
				if ((clause instanceof Symbol)
						&& (!(model.getAssignedSymbols().contains(clause)))) {
					// System.out.println("found unit clause - assigning");
					return new SymbolValuePair(new Symbol(
							((Symbol) clause).getValue()), true);
				}

				if (clause instanceof UnarySentence) {
					UnarySentence sentence = (UnarySentence) clause;
					Sentence negated = sentence.getNegated();
					if ((negated instanceof Symbol)
							&& (!(model.getAssignedSymbols().contains(negated)))) {
						// System.out.println("found unit clause type 2 -
						// assigning");
						return new SymbolValuePair(new Symbol(
								((Symbol) negated).getValue()), false);
					}
				}

			}

			return new SymbolValuePair();// failed to find any unit clause;

		}

		public static class SymbolValuePair {
			public Symbol symbol;// public to avoid unnecessary get and set

			// accessors

			public Boolean value;

			public SymbolValuePair() {
				// represents "No Symbol found with a boolean value that makes
				// all
				// its literals true
				symbol = null;
				value = null;
			}

			public SymbolValuePair(Symbol symbol, boolean bool) {
				// represents "Symbol found with a boolean value that makes all
				// its literals true
				this.symbol = symbol;
				value = new Boolean(bool);
			}

			public boolean notNull() {
				return (symbol != null) && (value != null);
			}

			@Override
			public String toString() {
				String symbolString, valueString;
				if (symbol == null) {
					symbolString = "NULL";
				} else {
					symbolString = symbol.toString();
				}
				if (value == null) {
					valueString = "NULL";
				} else {
					valueString = value.toString();
				}
				return symbolString + " -> " + valueString;
			}
		}

	}

	public static class LogicUtils {

		public static Sentence chainWith(String connector, List sentences) {
			if (sentences.size() == 0) {
				return null;
			} else if (sentences.size() == 1) {
				return (Sentence) sentences.get(0);
			} else {
				Sentence soFar = (Sentence) sentences.get(0);
				for (int i = 1; i < sentences.size(); i++) {
					Sentence next = (Sentence) sentences.get(i);
					soFar = new BinarySentence(connector, soFar, next);
				}
				return soFar;
			}
		}

		public static Sentence reorderCNFTransform(Set<Symbol> positiveSymbols,
				Set<Symbol> negativeSymbols) {
			List<Symbol> plusList = new Converter<Symbol>()
					.setToList(positiveSymbols);
			List<Symbol> minusList = new Converter<Symbol>()
					.setToList(negativeSymbols);
			Collections.sort(plusList, new SymbolComparator());
			Collections.sort(minusList, new SymbolComparator());

			List<Sentence> sentences = new ArrayList<Sentence>();
			for (int i = 0; i < positiveSymbols.size(); i++) {
				sentences.add(plusList.get(i));
			}
			for (int i = 0; i < negativeSymbols.size(); i++) {
				sentences.add(new UnarySentence(minusList.get(i)));
			}
			if (sentences.size() == 0) {
				return new Symbol("EMPTY_CLAUSE"); // == empty clause
			} else {
				return LogicUtils.chainWith("OR", sentences);
			}
		}

	}

	public static class CNFTransformer extends AbstractPLVisitor {
		@Override
		public Object visitBinarySentence(BinarySentence bs, Object arg) {
			if (bs.isBiconditional()) {
				return transformBiConditionalSentence(bs);
			} else if (bs.isImplication()) {
				return transformImpliedSentence(bs);
			} else if (bs.isOrSentence()
					&& (bs.firstTermIsAndSentence() || bs
							.secondTermIsAndSentence())) {
				return distributeOrOverAnd(bs);
			} else {
				return super.visitBinarySentence(bs, arg);
			}
		}

		@Override
		public Object visitNotSentence(UnarySentence us, Object arg) {
			return transformNotSentence(us);
		}

		public Sentence transform(Sentence s) {
			Sentence toTransform = s;
			while (!(toTransform.equals(step(toTransform)))) {
				toTransform = step(toTransform);
			}

			return toTransform;
		}

		private Sentence step(Sentence s) {
			return (Sentence) s.accept(this, null);
		}

		private Sentence transformBiConditionalSentence(BinarySentence bs) {
			Sentence first = new BinarySentence("=>", (Sentence) bs.getFirst()
					.accept(this, null), (Sentence) bs.getSecond().accept(this,
					null));
			Sentence second = new BinarySentence("=>", (Sentence) bs
					.getSecond().accept(this, null), (Sentence) bs.getFirst()
					.accept(this, null));
			return new BinarySentence("AND", first, second);
		}

		private Sentence transformImpliedSentence(BinarySentence bs) {
			Sentence first = new UnarySentence((Sentence) bs.getFirst().accept(
					this, null));
			return new BinarySentence("OR", first, (Sentence) bs.getSecond()
					.accept(this, null));
		}

		private Sentence transformNotSentence(UnarySentence us) {
			if (us.getNegated() instanceof UnarySentence) {
				return (Sentence) ((UnarySentence) us.getNegated())
						.getNegated().accept(this, null);
			} else if (us.getNegated() instanceof BinarySentence) {
				BinarySentence bs = (BinarySentence) us.getNegated();
				if (bs.isAndSentence()) {
					Sentence first = new UnarySentence((Sentence) bs.getFirst()
							.accept(this, null));
					Sentence second = new UnarySentence((Sentence) bs
							.getSecond().accept(this, null));
					return new BinarySentence("OR", first, second);
				} else if (bs.isOrSentence()) {
					Sentence first = new UnarySentence((Sentence) bs.getFirst()
							.accept(this, null));
					Sentence second = new UnarySentence((Sentence) bs
							.getSecond().accept(this, null));
					return new BinarySentence("AND", first, second);
				} else {
					return (Sentence) super.visitNotSentence(us, null);
				}
			} else {
				return (Sentence) super.visitNotSentence(us, null);
			}
		}

		private Sentence distributeOrOverAnd(BinarySentence bs) {
			BinarySentence andTerm = bs.firstTermIsAndSentence() ? (BinarySentence) bs
					.getFirst() : (BinarySentence) bs.getSecond();
			Sentence otherterm = bs.firstTermIsAndSentence() ? bs.getSecond()
					: bs.getFirst();
			// (alpha or (beta and gamma) = ((alpha or beta) and (alpha or
			// gamma))
			Sentence alpha = (Sentence) otherterm.accept(this, null);
			Sentence beta = (Sentence) andTerm.getFirst().accept(this, null);
			Sentence gamma = (Sentence) andTerm.getSecond().accept(this, null);
			Sentence distributed = new BinarySentence("AND",
					new BinarySentence("OR", alpha, beta), new BinarySentence(
							"OR", alpha, gamma));
			return distributed;
		}

	}

	public static class SymbolComparator implements Comparator {

		public int compare(Object symbol1, Object symbol2) {
			Symbol one = (Symbol) symbol1;
			Symbol two = (Symbol) symbol2;
			return one.getValue().compareTo(two.getValue());
		}

	}

	public static class PLResolution {

		public boolean plResolution(KnowledgeBase kb, String alpha) {
			return plResolution(kb, (Sentence) new PEParser().parse(alpha));
		}

		public boolean plResolution(KnowledgeBase kb, Sentence alpha) {
			Sentence kBAndNotAlpha = new BinarySentence("AND", kb.asSentence(),
					new UnarySentence(alpha));
			Set<Sentence> clauses = new CNFClauseGatherer()
					.getClausesFrom(new CNFTransformer()
							.transform(kBAndNotAlpha));
			clauses = filterOutClausesWithTwoComplementaryLiterals(clauses);
			Set<Sentence> newClauses = new HashSet<Sentence>();
			while (true) {
				List<List<Sentence>> pairs = getCombinationPairs(new Converter<Sentence>()
						.setToList(clauses));

				for (int i = 0; i < pairs.size(); i++) {
					List<Sentence> pair = pairs.get(i);
					// System.out.println("pair number" +
					// i+" of "+pairs.size());
					Set<Sentence> resolvents = plResolve(pair.get(0),
							pair.get(1));
					resolvents = filterOutClausesWithTwoComplementaryLiterals(resolvents);

					if (resolvents.contains(new Symbol("EMPTY_CLAUSE"))) {
						return false;
					}
					newClauses = new SetOps<Sentence>().union(newClauses,
							resolvents);
					// System.out.println("clauseslist size = "
					// +clauses.size());

				}
				if (new SetOps<Sentence>().intersection(newClauses, clauses)
						.size() == newClauses.size()) {// subset test
					return true;
				}
				clauses = new SetOps<Sentence>().union(newClauses, clauses);
				clauses = filterOutClausesWithTwoComplementaryLiterals(clauses);
			}

		}

		private Set<Sentence> filterOutClausesWithTwoComplementaryLiterals(
				Set<Sentence> clauses) {
			Set<Sentence> filtered = new HashSet<Sentence>();
			SymbolClassifier classifier = new SymbolClassifier();
			Iterator iter = clauses.iterator();
			while (iter.hasNext()) {
				Sentence clause = (Sentence) iter.next();
				Set<Symbol> positiveSymbols = classifier
						.getPositiveSymbolsIn(clause);
				Set<Symbol> negativeSymbols = classifier
						.getNegativeSymbolsIn(clause);
				if ((new SetOps<Symbol>().intersection(positiveSymbols,
						negativeSymbols).size() == 0)) {
					filtered.add(clause);
				}
			}
			return filtered;
		}

		public Set<Sentence> plResolve(Sentence clause1, Sentence clause2) {
			Set<Sentence> resolvents = new HashSet<Sentence>();
			ClauseSymbols cs = new ClauseSymbols(clause1, clause2);
			Iterator iter = cs.getComplementedSymbols().iterator();
			while (iter.hasNext()) {
				Symbol symbol = (Symbol) iter.next();
				resolvents.add(createResolventClause(cs, symbol));
			}

			return resolvents;
		}

		private Sentence createResolventClause(ClauseSymbols cs, Symbol toRemove) {
			List<Symbol> positiveSymbols = new Converter<Symbol>()
					.setToList(new SetOps<Symbol>().union(
							cs.clause1PositiveSymbols,
							cs.clause2PositiveSymbols));
			List<Symbol> negativeSymbols = new Converter<Symbol>()
					.setToList(new SetOps<Symbol>().union(
							cs.clause1NegativeSymbols,
							cs.clause2NegativeSymbols));
			if (positiveSymbols.contains(toRemove)) {
				positiveSymbols.remove(toRemove);
			}
			if (negativeSymbols.contains(toRemove)) {
				negativeSymbols.remove(toRemove);
			}

			Collections.sort(positiveSymbols, new SymbolComparator());
			Collections.sort(negativeSymbols, new SymbolComparator());

			List<Sentence> sentences = new ArrayList<Sentence>();
			for (int i = 0; i < positiveSymbols.size(); i++) {
				sentences.add(positiveSymbols.get(i));
			}
			for (int i = 0; i < negativeSymbols.size(); i++) {
				sentences.add(new UnarySentence(negativeSymbols.get(i)));
			}
			if (sentences.size() == 0) {
				return new Symbol("EMPTY_CLAUSE"); // == empty clause
			} else {
				return LogicUtils.chainWith("OR", sentences);
			}

		}

		private List<List<Sentence>> getCombinationPairs(
				List<Sentence> clausesList) {
			int odd = clausesList.size() % 2;
			int midpoint = 0;
			if (odd == 1) {
				midpoint = (clausesList.size() / 2) + 1;
			} else {
				midpoint = (clausesList.size() / 2);
			}

			List<List<Sentence>> pairs = new ArrayList<List<Sentence>>();
			for (int i = 0; i < clausesList.size(); i++) {
				for (int j = i; j < clausesList.size(); j++) {
					List<Sentence> pair = new ArrayList<Sentence>();
					Sentence first = clausesList.get(i);
					Sentence second = clausesList.get(j);

					if (!(first.equals(second))) {
						pair.add(first);
						pair.add(second);
						pairs.add(pair);
					}
				}
			}
			return pairs;
		}

		class ClauseSymbols {
			Set<Symbol> clause1Symbols, clause1PositiveSymbols,
					clause1NegativeSymbols;

			Set<Symbol> clause2Symbols, clause2PositiveSymbols,
					clause2NegativeSymbols;

			Set<Symbol> positiveInClause1NegativeInClause2,
					negativeInClause1PositiveInClause2;

			public ClauseSymbols(Sentence clause1, Sentence clause2) {

				SymbolClassifier classifier = new SymbolClassifier();

				clause1Symbols = classifier.getSymbolsIn(clause1);
				clause1PositiveSymbols = classifier
						.getPositiveSymbolsIn(clause1);
				clause1NegativeSymbols = classifier
						.getNegativeSymbolsIn(clause1);

				clause2Symbols = classifier.getSymbolsIn(clause2);
				clause2PositiveSymbols = classifier
						.getPositiveSymbolsIn(clause2);
				clause2NegativeSymbols = classifier
						.getNegativeSymbolsIn(clause2);

				positiveInClause1NegativeInClause2 = new SetOps<Symbol>()
						.intersection(clause1PositiveSymbols,
								clause2NegativeSymbols);
				negativeInClause1PositiveInClause2 = new SetOps<Symbol>()
						.intersection(clause1NegativeSymbols,
								clause2PositiveSymbols);

			}

			public Set getComplementedSymbols() {
				return new SetOps<Symbol>().union(
						positiveInClause1NegativeInClause2,
						negativeInClause1PositiveInClause2);
			}

		}

		public boolean plResolution(String kbs, String alphaString) {
			KnowledgeBase kb = new KnowledgeBase();
			kb.tell(kbs);
			Sentence alpha = (Sentence) new PEParser().parse(alphaString);
			return plResolution(kb, alpha);
		}
	}

	public static class SymbolCollector extends BasicTraverser {

		@Override
		public Object visitSymbol(Symbol s, Object arg) {
			Set<Symbol> symbolsCollectedSoFar = (Set) arg;
			symbolsCollectedSoFar.add(new Symbol(s.getValue()));
			return symbolsCollectedSoFar;
		}

		public Set<Symbol> getSymbolsIn(Sentence s) {
			if (s == null) {// empty knowledge bases == null fix this later
				return new HashSet<Symbol>();
			}
			return (Set<Symbol>) s.accept(this, new HashSet());
		}

	}

	public static class Util {
		public static final String NO = "No";

		public static final String YES = "Yes";

		private static Random r = new Random();

		public static <T> T first(List<T> l) {

			List<T> newList = new ArrayList<T>();
			for (T element : l) {
				newList.add(element);
			}
			return newList.get(0);
		}

		public static <T> List<T> rest(List<T> l) {
			List<T> newList = new ArrayList<T>();
			for (T element : l) {
				newList.add(element);
			}
			newList.remove(0);
			return newList;
		}

		public static boolean randomBoolean() {
			int trueOrFalse = r.nextInt(2);
			return (!(trueOrFalse == 0));
		}

		public static double[] normalize(double[] probDist) {
			int len = probDist.length;
			double total = 0.0;
			for (double d : probDist) {
				total = total + d;
			}

			double[] normalized = new double[len];
			if (total != 0) {
				for (int i = 0; i < len; i++) {
					normalized[i] = probDist[i] / total;
				}
			}
			double totalN = 0.0;
			for (double d : normalized) {
				totalN = totalN + d;
			}

			return normalized;
		}

		public static List<Double> normalize(List<Double> values) {
			double[] valuesAsArray = new double[values.size()];
			for (int i = 0; i < valuesAsArray.length; i++) {
				valuesAsArray[i] = values.get(i);
			}
			double[] normalized = normalize(valuesAsArray);
			List<Double> results = new ArrayList<Double>();
			for (int i = 0; i < normalized.length; i++) {
				results.add(normalized[i]);
			}
			return results;
		}

		public static int min(int i, int j) {
			return (i > j ? j : i);
		}

		public static int max(int i, int j) {
			return (i < j ? j : i);
		}

		public static int max(int i, int j, int k) {
			return max(max(i, j), k);
		}

		public static int min(int i, int j, int k) {
			return min(min(i, j), k);
		}

		public static <T> T selectRandomlyFromList(List<T> l) {
			int index = r.nextInt(l.size());
			return l.get(index);
		}

		public static <T> T mode(List<T> l) {
			Hashtable<T, Integer> hash = new Hashtable<T, Integer>();
			for (T obj : l) {
				if (hash.containsKey(obj)) {
					hash.put(obj, hash.get(obj).intValue() + 1);
				} else {
					hash.put(obj, 1);
				}
			}

			T maxkey = hash.keySet().iterator().next();
			for (T key : hash.keySet()) {
				if (hash.get(key) > hash.get(maxkey)) {
					maxkey = key;
				}
			}
			return maxkey;
		}

		public static String[] yesno() {
			return new String[] { YES, NO };
		}

		public static double log2(double d) {
			return Math.log(d) / Math.log(2);
		}

		public static double information(double[] probabilities) {
			double total = 0.0;
			for (double d : probabilities) {
				total += (-1.0 * log2(d) * d);
			}
			return total;
		}

		public static <T> List<T> removeFrom(List<T> list, T member) {
			List<T> newList = new ArrayList<T>();
			for (T s : list) {
				if (!(s.equals(member))) {
					newList.add(s);
				}
			}
			return newList;
		}

		public static <T extends Number> double sumOfSquares(List<T> list) {
			double accum = 0;
			for (T item : list) {
				accum = accum + (item.doubleValue() * item.doubleValue());
			}
			return accum;
		}

		public static String ntimes(String s, int n) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < n; i++) {
				buf.append(s);
			}
			return buf.toString();
		}

		public static void checkForNanOrInfinity(double d) {
			if (Double.isNaN(d)) {
				throw new RuntimeException("Not a Number");
			}
			if (Double.isInfinite(d)) {
				throw new RuntimeException("Infinite Number");
			}
		}

		public static int randomNumberBetween(int i, int j) {
			/* i,j both **inclusive** */
			return r.nextInt(j - i + 1) + i;
		}

		public static double calculateMean(List<Double> lst) {
			Double sum = 0.0;
			for (Double d : lst) {
				sum = sum + d.doubleValue();
			}
			return sum / lst.size();
		}

		public static double calculateStDev(List<Double> values, double mean) {

			int listSize = values.size();

			Double sumOfDiffSquared = 0.0;
			for (Double value : values) {
				double diffFromMean = value - mean;
				sumOfDiffSquared += ((diffFromMean * diffFromMean) / (listSize - 1));// division
				// moved
				// here
				// to
				// avoid
				// sum
				// becoming
				// too
				// big
				// if
				// this
				// doesn't
				// work
				// use
				// incremental
				// formulation

			}
			double variance = sumOfDiffSquared; // (listSize - 1); // assumes at
			// least 2
			// members in
			// list
			return Math.sqrt(variance);
		}

		public static List<Double> normalizeFromMeanAndStdev(
				List<Double> values, double mean, double stdev) {
			List<Double> normalized = new ArrayList<Double>();
			for (Double d : values) {
				normalized.add((d - mean) / stdev);
			}
			return normalized;
		}

		public static double generateRandomDoubleBetween(double lowerLimit,
				double upperLimit) {

			return lowerLimit + ((upperLimit - lowerLimit) * r.nextDouble());
		}

	}

	public static abstract class Sentence implements ParseTreeNode {

		public abstract Object accept(PLVisitor plv, Object arg);

	}

	public static abstract class Parser {

		protected Lexer lexer;

		protected Token[] lookAheadBuffer;

		protected int lookAhead = 3;

		protected void fillLookAheadBuffer() {
			for (int i = 0; i < lookAhead; i++) {
				lookAheadBuffer[i] = lexer.nextToken();
			}
		}

		protected Token lookAhead(int i) {
			return lookAheadBuffer[i - 1];
		}

		protected void consume() {
			loadNextTokenFromInput();
		}

		protected void loadNextTokenFromInput() {

			boolean eoiEncountered = false;
			for (int i = 0; i < lookAhead - 1; i++) {

				lookAheadBuffer[i] = lookAheadBuffer[i + 1];
				if (isEndOfInput(lookAheadBuffer[i])) {
					eoiEncountered = true;
					break;
				}
			}
			if (!eoiEncountered) {
				try {
					lookAheadBuffer[lookAhead - 1] = lexer.nextToken();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		protected boolean isEndOfInput(Token t) {
			return (t.getType() == LogicTokenTypes.EOI);
		}

		protected void match(String terminalSymbol) {
			if (lookAhead(1).getText().equals(terminalSymbol)) {
				consume();
			} else {
				throw new RuntimeException(
						"Syntax error detected at match. Expected "
								+ terminalSymbol + " but got "
								+ lookAhead(1).getText());
			}

		}

		public abstract ParseTreeNode parse(String input);

	}

	public interface ParseTreeNode {

	}

	public interface LogicTokenTypes {
		static final int SYMBOL = 1;

		static final int LPAREN = 2;

		static final int RPAREN = 3;

		static final int COMMA = 4;

		static final int CONNECTOR = 5;

		static final int QUANTIFIER = 6;

		static final int PREDICATE = 7;

		static final int FUNCTION = 8;

		static final int VARIABLE = 9;

		static final int CONSTANT = 10;

		static final int TRUE = 11;

		static final int FALSE = 12;

		static final int EQUALS = 13;

		static final int WHITESPACE = 1000;

		static final int EOI = 9999;

	}

	public static class Token {
		private String text;

		private int type;

		public Token(int type, String text) {
			this.type = type;
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public int getType() {
			return type;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}
			if ((o == null) || (this.getClass() != o.getClass())) {
				return false;
			}
			Token other = (Token) o;
			return ((other.type == type) && (other.text.equals(text)));
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 37 * result + type;
			result = 37 * result + text.hashCode();
			return 17;
		}

		@Override
		public String toString() {
			return "[ " + type + " " + text + " ]";
		}

	}

	public interface Visitor {

	}

	public static abstract class Lexer {
		protected abstract Token nextToken();

		protected Reader input;

		protected int lookAhead = 1;

		protected int[] lookAheadBuffer;

		public void setInput(String inputString) {
			lookAheadBuffer = new int[lookAhead];
			this.input = new StringReader(inputString);
			fillLookAheadBuffer();
		}

		protected void fillLookAheadBuffer() {
			try {
				lookAheadBuffer[0] = (char) input.read();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		protected char lookAhead(int position) {
			return (char) lookAheadBuffer[position - 1];
		}

		protected boolean isEndOfFile(int i) {
			return (-1 == i);
		}

		protected void loadNextCharacterFromInput() {

			boolean eofEncountered = false;
			for (int i = 0; i < lookAhead - 1; i++) {

				lookAheadBuffer[i] = lookAheadBuffer[i + 1];
				if (isEndOfFile(lookAheadBuffer[i])) {
					eofEncountered = true;
					break;
				}
			}
			if (!eofEncountered) {
				try {
					lookAheadBuffer[lookAhead - 1] = input.read();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		protected void consume() {
			loadNextCharacterFromInput();
		}

		public void clear() {
			this.input = null;
			lookAheadBuffer = null;

		}

	}

	public static class Symbol extends AtomicSentence {
		private String value;

		public Symbol(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}
			if ((o == null) || (this.getClass() != o.getClass())) {
				return false;
			}
			Symbol sym = (Symbol) o;
			return (sym.getValue().equals(getValue()));

		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 37 * result + value.hashCode();
			return result;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return getValue();
		}

		@Override
		public Object accept(PLVisitor plv, Object arg) {
			return plv.visitSymbol(this, arg);
		}

	}

	public static class TrueSentence extends AtomicSentence {

		@Override
		public String toString() {
			return "TRUE";
		}

		@Override
		public Object accept(PLVisitor plv, Object arg) {
			return plv.visitTrueSentence(this, arg);
		}
	}

	public static class UnarySentence extends ComplexSentence {
		private Sentence negated;

		public Sentence getNegated() {
			return negated;
		}

		public UnarySentence(Sentence negated) {
			this.negated = negated;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}
			if ((o == null) || (this.getClass() != o.getClass())) {
				return false;
			}
			UnarySentence ns = (UnarySentence) o;
			return (ns.negated.equals(negated));

		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 37 * result + negated.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return " ( NOT " + negated.toString() + " ) ";
		}

		@Override
		public Object accept(PLVisitor plv, Object arg) {
			return plv.visitNotSentence(this, arg);
		}
	}

	public static class NegativeSymbolCollector extends BasicTraverser {
		@Override
		public Object visitNotSentence(UnarySentence ns, Object arg) {
			Set<Symbol> s = (Set<Symbol>) arg;
			if (ns.getNegated() instanceof Symbol) {
				s.add((Symbol) ns.getNegated());
			} else {
				s = new SetOps<Symbol>().union(s, (Set<Symbol>) ns.getNegated()
						.accept(this, arg));
			}
			return s;
		}

		public Set<Symbol> getNegativeSymbolsIn(Sentence s) {
			return (Set<Symbol>) s.accept(this, new HashSet());
		}

	}

	public static class PositiveSymbolCollector extends BasicTraverser {
		@Override
		public Object visitSymbol(Symbol symbol, Object arg) {
			Set<Symbol> s = (Set<Symbol>) arg;
			s.add(symbol);// add ALL symbols not discarded by the
							// visitNotSentence
			// mathod
			return arg;
		}

		@Override
		public Object visitNotSentence(UnarySentence ns, Object arg) {
			Set<Symbol> s = (Set<Symbol>) arg;
			if (ns.getNegated() instanceof Symbol) {
				// do nothing .do NOT add a negated Symbol
			} else {
				s = new SetOps<Symbol>().union(s, (Set<Symbol>) ns.getNegated()
						.accept(this, arg));
			}
			return s;
		}

		public Set<Symbol> getPositiveSymbolsIn(Sentence sentence) {
			return (Set<Symbol>) sentence.accept(this, new HashSet<Symbol>());
		}
	}

	public static class SymbolClassifier {

		public Set<Symbol> getPositiveSymbolsIn(Sentence sentence) {
			return new PositiveSymbolCollector().getPositiveSymbolsIn(sentence);
		}

		public Set<Symbol> getNegativeSymbolsIn(Sentence sentence) {
			return new NegativeSymbolCollector().getNegativeSymbolsIn(sentence);
		}

		public Set<Symbol> getPureNegativeSymbolsIn(Sentence sentence) {
			Set<Symbol> allNegatives = getNegativeSymbolsIn(sentence);
			Set<Symbol> allPositives = getPositiveSymbolsIn(sentence);
			return new SetOps<Symbol>().difference(allNegatives, allPositives);
		}

		public Set<Symbol> getPurePositiveSymbolsIn(Sentence sentence) {
			Set<Symbol> allNegatives = getNegativeSymbolsIn(sentence);
			Set<Symbol> allPositives = getPositiveSymbolsIn(sentence);
			return new SetOps<Symbol>().difference(allPositives, allNegatives);
		}

		public Set<Symbol> getPureSymbolsIn(Sentence sentence) {
			Set<Symbol> allPureNegatives = getPureNegativeSymbolsIn(sentence);
			Set<Symbol> allPurePositives = getPurePositiveSymbolsIn(sentence);
			return new SetOps<Symbol>().union(allPurePositives,
					allPureNegatives);
		}

		public Set<Symbol> getImpureSymbolsIn(Sentence sentence) {
			Set<Symbol> allNegatives = getNegativeSymbolsIn(sentence);
			Set<Symbol> allPositives = getPositiveSymbolsIn(sentence);
			return new SetOps<Symbol>()
					.intersection(allPositives, allNegatives);
		}

		public Set<Symbol> getSymbolsIn(Sentence sentence) {
			return new SymbolCollector().getSymbolsIn(sentence);
		}

	}

	public interface PLVisitor extends Visitor {
		public Object visitSymbol(Symbol s, Object arg);

		public Object visitTrueSentence(TrueSentence ts, Object arg);

		public Object visitFalseSentence(FalseSentence fs, Object arg);

		public Object visitNotSentence(UnarySentence fs, Object arg);

		public Object visitBinarySentence(BinarySentence fs, Object arg);

		public Object visitMultiSentence(MultiSentence fs, Object arg);
	}

	public static abstract class ComplexSentence extends Sentence {

	}

	public static class BinarySentence extends ComplexSentence {
		private String operator;

		private Sentence first;

		private Sentence second;

		public BinarySentence(String operator, Sentence first, Sentence second) {
			this.operator = operator;
			this.first = first;
			this.second = second;

		}

		public Sentence getFirst() {
			return first;
		}

		public String getOperator() {
			return operator;
		}

		public Sentence getSecond() {
			return second;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}
			if ((o == null) || (this.getClass() != o.getClass())) {
				return false;
			}
			BinarySentence bs = (BinarySentence) o;
			return ((bs.getOperator().equals(getOperator()))
					&& (bs.getFirst().equals(first)) && (bs.getSecond()
					.equals(second)));

		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 37 * result + first.hashCode();
			result = 37 * result + second.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return " ( " + first.toString() + " " + operator + " "
					+ second.toString() + " )";
		}

		@Override
		public Object accept(PLVisitor plv, Object arg) {
			return plv.visitBinarySentence(this, arg);
		}

		public boolean isOrSentence() {
			return (getOperator().equals("OR"));
		}

		public boolean isAndSentence() {
			return (getOperator().equals("AND"));
		}

		public boolean isImplication() {
			return (getOperator().equals("=>"));
		}

		public boolean isBiconditional() {
			return (getOperator().equals("<=>"));
		}

		public boolean firstTermIsAndSentence() {
			return (getFirst() instanceof BinarySentence)
					&& (((BinarySentence) getFirst()).isAndSentence());
		}

		public boolean secondTermIsAndSentence() {
			return (getSecond() instanceof BinarySentence)
					&& (((BinarySentence) getSecond()).isAndSentence());
		}
	}

	public static abstract class AtomicSentence extends Sentence {

	}

	public static class Model implements PLVisitor {

		Hashtable<String, Boolean> h = new Hashtable<String, Boolean>();

		public Boolean getStatus(Symbol symbol) {
			Object status = h.get(symbol.getValue());
			if (status != null) {
				return (Boolean) status;
			}
			return null;
		}

		public boolean isTrue(Symbol symbol) {
			Object status = h.get(symbol.getValue());
			if (status != null) {
				return ((Boolean) status).booleanValue();
			}
			return false;
		}

		public boolean isFalse(Symbol symbol) {
			Object status = h.get(symbol.getValue());
			if (status != null) {
				return !((Boolean) status).booleanValue();
			}
			return false;
		}

		private boolean isUnknown(Symbol s) {
			Object o = h.get(s.getValue());
			return (o == null);

		}

		public Model extend(Symbol symbol, boolean b) {
			Model m = new Model();
			return extend(symbol.getValue(), b);
		}

		public Model extend(String s, boolean b) {
			Model m = new Model();
			Iterator<String> i = this.h.keySet().iterator();
			while (i.hasNext()) {
				String key = i.next();
				Boolean value = h.get(key);
				String newKey = new String((key).toCharArray());
				if (value == null) {
					throw new RuntimeException();
				}
				m.h.put(key, value);
			}
			m.h.put(s, new Boolean(b));
			return m;
		}

		public void print(int container, int chemical, PrintWriter outputresult) 
		{
			for (int i = 0; i < container; i++) {
				for (int j = 0; j < chemical; j++) {
					if(h.get("X" + j + i)==true)
					{
						outputresult.print("1 ");
					}
					else
					{
						outputresult.print("0 ");
					}
				}
				outputresult.println();
			}
			
		}

		public boolean isTrue(Sentence clause) {
			Object result = clause.accept(this, null);
			return (result == null) ? false
					: ((Boolean) result).booleanValue() == true;
		}

		public boolean isFalse(Sentence clause) {
			Object o = clause.accept(this, null);
			return (o != null) ? ((Boolean) o).booleanValue() == false : false;
		}

		public boolean isUnknown(Sentence clause) { // TODO TEST WELL
			Object o = clause.accept(this, null);
			return (o == null);
		}

		public Model flip(Symbol s, int chemical,int container, Model m) {
			int currentChem=findthechemical(s,chemical,container);
			int currentCon=findthecontainer(s,chemical,container);
			int nextCon=0;
			String temp;
			Random ran = new Random();
			if (isTrue(s)) 
			{
				nextCon=ran.nextInt(container);
				while(currentCon==nextCon)
				{
					nextCon=ran.nextInt(container);
				}
				
				for(int con =0;con<container;con++)
				{
					if(con==nextCon)
					{
						m = m.extend("X"+currentChem+con, true);
						
						//m = m.extend("-X"+j+temp, false);
					}
					else
					{
						m = m.extend("X"+currentChem+con, false);
						//m = m.extend("-X"+j+temp, true);
					}
				}
			}
			else
			{
				for(int con=0;con<container;con++)
				{
					temp="X"+currentChem+con;
					m = m.extend(temp,false);
				}
				m = m.extend(s,true);
			}
			return m;
		}
		
		public int findthechemical(Symbol s, int chemical,int container)
		{
			int thechemical=0;
			for(int i = 0 ; i < container;i++)
			{
				for(int j = 0;j<chemical;j++)
				{
					if(s.toString().equals("X"+j+i))
					{
						thechemical=j;
						break;
					}
						
				}
			}
			return thechemical;
		}/*
		public Model flip(Symbol s) {
			if (isTrue(s)) {
				return extend(s, false);
			}
			if (isFalse(s)) {
				return extend(s, true);
			}
			return this;
		}*/
		public int findthecontainer(Symbol s, int chemical,int container)
		{
			int thecontainer=0;
			for(int i = 0 ; i < container;i++)
			{
				for(int j = 0;j<chemical;j++)
				{
					if(s.toString()=="X"+i+j)
					{
						thecontainer=i;
						break;
					}
						
				}
			}
			return thecontainer;
		}
		public Model yesnoflip(int currentChem,int nextCon,int chemical,int container, int[][]arr, Model m)
		{
			
			return m;
		}

		@Override
		public String toString() {
			return h.toString();
		}

		// VISITOR METHODS
		public Object visitSymbol(Symbol s, Object arg) {
			return getStatus(s);
		}

		public Object visitTrueSentence(TrueSentence ts, Object arg) {
			return Boolean.TRUE;
		}

		public Object visitFalseSentence(FalseSentence fs, Object arg) {
			return Boolean.FALSE;
		}

		public Object visitNotSentence(UnarySentence fs, Object arg) {
			Object negatedValue = fs.getNegated().accept(this, null);
			if (negatedValue != null) {
				return new Boolean(!((Boolean) negatedValue).booleanValue());
			} else {
				return null;
			}
		}

		public Object visitBinarySentence(BinarySentence bs, Object arg) {
			Object firstValue = bs.getFirst().accept(this, null);
			Object secondValue = bs.getSecond().accept(this, null);
			if ((firstValue == null) || (secondValue == null)) { // strictly not
				// true for or/and
				// -FIX later
				return null;
			} else {
				String operator = bs.getOperator();
				if (operator.equals("AND")) {
					return evaluateAnd((Boolean) firstValue,
							(Boolean) secondValue);
				}
				if (operator.equals("OR")) {
					return evaluateOr((Boolean) firstValue,
							(Boolean) secondValue);
				}
				if (operator.equals("=>")) {
					return evaluateImplied((Boolean) firstValue,
							(Boolean) secondValue);
				}
				if (operator.equals("<=>")) {
					return evaluateBiConditional((Boolean) firstValue,
							(Boolean) secondValue);
				}
				return null;
			}
		}

		public Object visitMultiSentence(MultiSentence fs, Object argd) {
			// TODO remove this?
			return null;
		}

		private Boolean evaluateAnd(Boolean firstValue, Boolean secondValue) {
			if ((firstValue.equals(Boolean.TRUE))
					&& (secondValue.equals(Boolean.TRUE))) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}

		private Boolean evaluateOr(Boolean firstValue, Boolean secondValue) {
			if ((firstValue.equals(Boolean.TRUE))
					|| (secondValue.equals(Boolean.TRUE))) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}

		private Boolean evaluateImplied(Boolean firstValue, Boolean secondValue) {
			if ((firstValue.equals(Boolean.TRUE))
					&& (secondValue.equals(Boolean.FALSE))) {
				return Boolean.FALSE;
			} else {
				return Boolean.TRUE;
			}
		}

		private Boolean evaluateBiConditional(Boolean firstValue,
				Boolean secondValue) {
			if (firstValue.equals(secondValue)) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}

		public Set<Symbol> getAssignedSymbols() {
			Set<Symbol> set = new HashSet<Symbol>();
			Iterator i = this.h.keySet().iterator();
			while (i.hasNext()) {
				Symbol key = new Symbol((String) i.next());
				if (!(isUnknown(key))) {
					set.add(key);
				}
			}
			return set;
		}

		public boolean matches(String variable, boolean value) {
			if (value) {
				return isTrue(new Symbol(variable));
			} else if (!(value)) {
				return isFalse(new Symbol(variable));
			}
			return false;
		}

	}

	public static class WalkSAT {
		private Model myModel;
		public int iterations;
		private Random random = new Random();

		public Model findModelFor(String logicalSentence, int numberOfFlips,double probabilityOfRandomWalk,int chemical,int container) {
			myModel = new Model();
			Sentence s = (Sentence) new PEParser().parse(logicalSentence);

			CNFTransformer transformer = new CNFTransformer();
			// System.out.println("transformer:" + transformer);
			CNFClauseGatherer clauseGatherer = new CNFClauseGatherer();
			// System.out.println("clauseGatherer:" + clauseGatherer);
			SymbolCollector sc = new SymbolCollector();
			// System.out.println("sc:" + sc);

			List symbols = new Converter<Symbol>()
					.setToList(sc.getSymbolsIn(s));
/*
			for (int i = 0; i < symbols.size(); i++) {
				Symbol sym = (Symbol) symbols.get(i);
				myModel = myModel.extend(sym, Util.randomBoolean());
			}*/
			Random ran = new Random();
			int c=0;
			for (int chem = 0; chem < chemical; chem++) 
			{
				c=ran.nextInt(container);
				for(int con =0;con<container;con++)
				{
					if(con==c)
					{
						myModel = myModel.extend("X"+chem+con, true);
						//myModel = myModel.extend("-X"+con+chem, false);
					}
					else
					{
						myModel = myModel.extend("X"+chem+con, false);
						//myModel = myModel.extend("-X"+con+chem, true);
					}
				}
			}
			// System.out.println("myModel:" + myModel);

			List<Sentence> clauses = new Converter<Sentence>()
					.setToList(clauseGatherer.getClausesFrom(transformer
							.transform(s)));
			// System.out.println("clauses:" + clauses);

			iterations = 0;
			for (int i = 0; i < numberOfFlips; i++) {
				iterations++;
				if (getNumberOfClausesSatisfiedIn(
						new Converter<Sentence>().listToSet(clauses), myModel) == clauses
						.size()) {
					return myModel;
				}
				Sentence clause = clauses.get(random.nextInt(clauses.size()));
				// System.out.println("clause:" + clause);

				List<Symbol> symbolsInClause = new Converter<Symbol>()
						.setToList(sc.getSymbolsIn(clause));
				if (random.nextDouble() >= probabilityOfRandomWalk) {
					Symbol randomSymbol = symbolsInClause.get(random
							.nextInt(symbolsInClause.size()));
					myModel = myModel.flip(randomSymbol,chemical,container, myModel);
					//myModel = myModel.flip(randomSymbol);
				} else {
					Symbol symbolToFlip = getSymbolWhoseFlipMaximisesSatisfiedClauses(
							new Converter<Sentence>().listToSet(clauses),
							symbolsInClause, myModel,chemical,container);
					myModel = myModel.flip(symbolToFlip,chemical,container, myModel);
					//myModel = myModel.flip(symbolToFlip);
				}

			}
			return null;
		}

		private Symbol getSymbolWhoseFlipMaximisesSatisfiedClauses(
				Set<Sentence> clauses, List<Symbol> symbols, Model model,int chemical,int container) {
			if (symbols.size() > 0) {
				Symbol retVal = symbols.get(0);
				int maxClausesSatisfied = 0;
				for (int i = 0; i < symbols.size(); i++) {
					Symbol sym = symbols.get(i);
					if (getNumberOfClausesSatisfiedIn(clauses, model.flip(sym,chemical,container, myModel)) > maxClausesSatisfied) {
						retVal = sym;
						maxClausesSatisfied = getNumberOfClausesSatisfiedIn(
								clauses, model.flip(sym,chemical,container, myModel));
					}/*
					if (getNumberOfClausesSatisfiedIn(clauses, model.flip(sym)) > maxClausesSatisfied) {
						retVal = sym;
						maxClausesSatisfied = getNumberOfClausesSatisfiedIn(
								clauses, model.flip(sym));
					}*/
				}
				return retVal;
			} else {
				return null;
			}

		}

		private int getNumberOfClausesSatisfiedIn(Set clauses, Model model) {
			int retVal = 0;
			Iterator i = clauses.iterator();
			while (i.hasNext()) {
				Sentence s = (Sentence) i.next();
				if (model.isTrue(s)) {
					retVal += 1;
				}
			}
			return retVal;
		}

		public int getIterations() {
			return iterations;
		}
	}

	public static class Converter<T> {

		public List<T> setToList(Set<T> set) {
			List<T> retVal = new ArrayList<T>(set);
			return retVal;
		}

		public Set<T> listToSet(List<T> l) {

			Set<T> retVal = new HashSet<T>(l);
			return retVal;

		}

	}


}
