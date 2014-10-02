package statemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

import statemachine.fa.impl.StateMachineImpl;
import automata.DFA;
import automata.NFA;
import automata.State;
import automata.Symbol;
import automata.impl.Automaton;
import automata.impl.Automaton.NfaBuilder;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings("nls")
public class NfaTest {

  @Test
  public void test() {
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
    
    
//    nfa.accept("A");
//    nfa.accept("B");
//    nfa.accept("A");
//    
//    nfa.isOutputState();
    
//    fail("Not yet implemented");
  }
  
  @Test
  public void nfaEpsilonTest()  {
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
  public void nfa2dfaTest1()  {
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
  public void nfaEpsilon2Test() {
    NfaBuilder builder = Automaton.builder();
    
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
    builder.addOutputState(D);

    builder.addTransition(A, B, Se);
    builder.addTransition(A, C, Se);
    builder.addTransition(B, D, Se);
    
    builder.addTransition(A, B, S0);
    builder.addTransition(B, C, S0);
    builder.addTransition(C, D, S1);

    
    NFA nfa = builder.build();
    System.out.println(nfa);
    
    DFA dfa = nfa.toDFA();
    System.out.println(dfa);
    dfaTester(dfa);
    
    assertTrue(dfa.outputStates().contains(dfa.initialState()));
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
