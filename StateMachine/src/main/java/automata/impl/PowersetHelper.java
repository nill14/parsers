package automata.impl;

import java.util.Deque;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;

import automata.DFA;
import automata.State;
import automata.Symbol;
import automata.impl.Automaton.NfaBuilder;
import automata.model.CompoundState;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PowersetHelper {

  private NfaBuilder builder;
  private Automaton nfa;
  private Set<CompoundState> markedStates = Sets.newHashSet();

  public PowersetHelper(NfaBuilder builder, Automaton nfa) {
    this.builder = builder;
    this.nfa = nfa;
  }
  

  public DFA create() {
    
    CompoundState initialMove = initialMove(nfa.initialState());
    
    Deque<CompoundState> followUps = Lists.newLinkedList();
    followUps.add(initialMove);
    
    while (!followUps.isEmpty()) {

      CompoundState sourceState = followUps.poll();
      if (!markedStates.contains(sourceState)) {
        
        SortedSet<Symbol> symbols = symbols(sourceState);
        
        for (Symbol symbol : symbols) {
          CompoundState targetState = followUpMove(sourceState, symbol);
          builder.addTransition(sourceState, targetState, symbol);
          followUps.add(targetState);
        }
        
        markDone(sourceState);
      }
      
    }
    
    
    builder.setInputState(initialMove);
    markedStates.stream().filter(this::isOutputState).forEach(builder::addOutputState);

    return (DFA) builder.build();
  }

  
  private Stream<State> nfaMove(State state, Symbol symbol) {
    return nfa.transitions(state, symbol).stream();
  }
  
  private CompoundState initialMove(State initialState) {
    return newSubset( closure(initialState) );
  }  
  
  private CompoundState followUpMove(CompoundState state, Symbol symbol) {
    return newSubset( closure( dfaMove(state, symbol) ) );
  }  
  
  private Stream<State> closure(State state) {
    Stream<State> closure = nfaMove(state, Symbol.epsilon())
          .flatMap(s -> closure(s));
    return Stream.concat(Stream.of(state), closure);   
  }
  
  private Stream<State> closure(Stream<State> moves) {
    return moves.flatMap(s -> closure(s));
  }
  
  private CompoundState newSubset(Stream<State> moves) {
    Set<State> states = ImmutableSet.copyOf(moves.iterator());
    return CompoundState.from(states);   
  }
  
  private SortedSet<Symbol> symbols(CompoundState state) {
    Stream<Symbol> symbols = state.states().stream()
          .flatMap(s -> nfa.symbols(s))
          .filter(s -> !s.isEpsilon());
    return ImmutableSortedSet.copyOf(symbols.iterator());
  }
  
  private void markDone(CompoundState state) {
    markedStates.add(state);
  }
  
  private Stream<State> dfaMove(CompoundState state, Symbol symbol) {
    return state.states().stream()
        .flatMap(s -> nfaMove(s, symbol));
  }
  
  private boolean isOutputState(CompoundState state) {
    return state.states().stream().anyMatch(nfa::isOutputState);
  }
  
}
