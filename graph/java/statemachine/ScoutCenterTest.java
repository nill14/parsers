package statemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import parser.Lexeme;
import parser.LexicalAnalyzer;
import parser.Tokenizer;
import statemachine.fa.impl.StateMachineImpl;
import util.FileReader;
import automata.DFA;
import automata.FiniteAutomaton;
import automata.NFA;
import automata.State;
import automata.Symbol;
import automata.impl.Automaton;
import automata.impl.Automaton.NfaBuilder;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings("nls")
public class ScoutCenterTest {


  
  @Test
  public void test() throws IOException {
    NfaBuilder builder = Automaton.builder();
    
    State A = State.of("A");
    State B = State.of("B");
    
    Symbol S0 = Symbol.of("0");
    Symbol S1 = Symbol.of("1");
    
    builder.addState(A);
    builder.addState(B);
    
    builder.addTransition(A, A, S0);
    builder.addTransition(A, A, S1);
    builder.addTransition(A, B, S1);

    builder.setInputState(A);
    builder.addOutputState(B);
    
    NFA nfa = builder.build();
    System.out.println(nfa);
    
    assertEquals(nodeSet(A), nfa.transitions(A, S0));
    assertEquals(nodeSet(A, B), nfa.transitions(A, S1));
    assertEquals(nodeSet(), nfa.transitions(B, S1));
    
    DFA dfa = nfa.toDFA();
    System.out.println(dfa);
    dfaTester(dfa);
    
    StateMachine stateMachine = new StateMachineImpl(dfa);
    
    
    assertTrue(stateMachine.acceptInput(S0, S1, S1, S1));
    
    InputStream is = new FileReader().openClasspathResource(getClass(), "scoutCenter.txt");
    List<Lexeme> tokens = new FileReader().readTokensFromProperties(getClass(), "parser.properties");
    LexicalAnalyzer inputProcessor = new Tokenizer(tokens);
    Scanner scanner = new Scanner(is);
    
    
    inputProcessor.tokenize(scanner).toArray();
//    nfa.accept("A");
//    nfa.accept("B");
//    nfa.accept("A");
//    
//    nfa.isOutputState();
    
//    fail("Not yet implemented");
  }
  
  @Test
  public void nfaEpsilonTest() {
    NfaBuilder builder = Automaton.builder();
    //see http://en.wikipedia.org/wiki/Powerset_construction example
    
    State A = State.of("A");
    State B = State.of("B");
    State C = State.of("C");
    State D = State.of("D");
    
    Symbol S0 = Symbol.of("0");
    Symbol S1 = Symbol.of("1");
    Symbol Se = Symbol.epsilon();
    
    builder.addState(A);
    builder.addState(B);
    builder.addState(C);
    builder.addState(D);
    
    builder.setInputState(A);
    builder.addOutputState(C);
    builder.addOutputState(D);

    builder.addTransition(A, B, S0);
    builder.addTransition(A, C, Se);
    builder.addTransition(B, B, S1);
    builder.addTransition(B, D, S1);
    builder.addTransition(C, B, Se);
    builder.addTransition(C, D, S0);
    builder.addTransition(D, C, S0);

    
    NFA nfa = builder.build();
    System.out.println(nfa);
    
    DFA dfa = nfa.toDFA();
    System.out.println(dfa);
    dfaTester(dfa);
    
    
    
//    nfa.accept("A");
//    nfa.accept("B");
//    nfa.accept("A");
//    
//    nfa.isOutputState();
    
//    fail("Not yet implemented");
  }  

  @Test
  public void inputProcessorTest() throws IOException {
    InputStream is = new FileReader().openClasspathResource(getClass(), "scoutCenter.txt");
    Scanner scanner = new Scanner(is);
    
    
    List<Lexeme> lexemes = new FileReader().readTokensFromProperties(getClass(), "parser.properties");
    LexicalAnalyzer inputProcessor = new Tokenizer(lexemes);
    
    inputProcessor.tokenize(scanner)
        .filter(l -> !"WHITE_SPACE".equals(l.symbol().name()))
        .forEach(l -> System.out.println(l));
//    .forEach(s -> System.out.println(s));
  }
  
  @Test
  public void nfa2dfaTest1(){
    NfaBuilder builder = Automaton.builder();
    
    State A = State.of("A");
    State B = State.of("B");
    State C = State.of("C");
    State D = State.of("D");
    
    Symbol S0 = Symbol.of("0");
    Symbol S1 = Symbol.of("1");
    
    builder.addState(A);
    builder.addState(B);
    builder.addState(C);
    builder.addState(D);
    
    builder.setInputState(A);
    builder.addOutputState(D);

    builder.addTransition(A, A, S0);
    builder.addTransition(A, A, S1);
    
    builder.addTransition(A, B, S1);
    builder.addTransition(B, C, S1);
    builder.addTransition(C, D, S1);

    
    NFA nfa = builder.build();
    System.out.println(nfa);
    
    DFA dfa = nfa.toDFA();
    System.out.println(dfa);
    dfaTester(dfa);
    
    StateMachine stateMachine = new StateMachineImpl(dfa);
    
    
    assertTrue(stateMachine.acceptInput(S0, S1, S1, S1));
    assertFalse(stateMachine.acceptInput(S0, S1, S1, S0));
    assertTrue(stateMachine.acceptInput(S0, S1, S1, S1, S1, S1));
    
//    nfa.accept("A");
//    nfa.accept("B");
//    nfa.accept("A");
//    
//    nfa.isOutputState();
    
//    fail("Not yet implemented");
  }  
  
  @Test
  public void exactTest() {
    Lexeme exactToken = Lexeme.exact("EXACT", "word one");
    assertTrue(exactToken.pattern().matcher("word one blabla").find());
    
    assertFalse(exactToken.pattern().matcher("blabla word one blabla").find());
    
    assertFalse(exactToken.pattern().matcher("word onee blabla").find());
  }
  
  
  @Test
  public void whitespaceTest() {
    Lexeme whiteSpaceToken = Lexeme.regex("WHITESPACE", "\\s+");
    assertTrue(whiteSpaceToken.pattern().matcher("  sfa sdf").find());
    assertFalse(whiteSpaceToken.pattern().matcher("blabla sfs sdfe").find());
    assertTrue(whiteSpaceToken.pattern().matcher("ab  sfa sdf").find(2));
  }
  
  @Test
  public void wordTest() {
    Lexeme wordToken = Lexeme.regex("WORD", "\\S+");
    assertTrue(wordToken.pattern().matcher("abVzťah sfa wer w").find());
    assertTrue(wordToken.pattern().matcher("ab Vzťah sfa wer w").find(3));
  }
  
  @Test
  public void word2Test() {
    Lexeme wordToken = Lexeme.exact("WORD", "ab");
    assertFalse(wordToken.pattern().matcher("abc sfa: wer: w:").find());
    assertFalse(wordToken.pattern().matcher("ab Vsfa wab w").find(3));
  }
  @Test
  public void delimTest() {
    Pattern delimPattern = Pattern.compile("$");
    String input = "abc cde wef";
    Matcher matcher = delimPattern.matcher(input);
    assertTrue(matcher.find());
    assertEquals(matcher.start(), input.length());
  }
  
  private Set<State> nodeSet(State ... nodes) {
    return ImmutableSet.copyOf(Arrays.asList(nodes));
  }
  
  private void dfaTester(DFA dfa){
    Set<Symbol> symbols = dfa.alphabet();
    Set<State> states = dfa.states();

    assertFalse(symbols.contains(Symbol.epsilon()));
    
    for (State state : states) {
      for (Symbol symbol : symbols) {
        dfa.transition(state, symbol);
      }
    }
  }
   
  

  

  

}


