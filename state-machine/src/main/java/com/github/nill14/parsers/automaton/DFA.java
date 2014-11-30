package com.github.nill14.parsers.automaton;

import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.github.nill14.parsers.automaton.model.Transition;
import com.github.nill14.parsers.graph.DirectedGraph;

public interface DFA<E extends Comparable<? super E>, A extends Comparable<? super A>> {

  boolean accept(State<E> state, Symbol<A> symbol);
  
  State<E> initialState();
  
  Set<State<E>> outputStates();
  
  Set<State<E>> states();
  
  SortedSet<Symbol<A>> alphabet();
  
  DirectedGraph<State<E>, Transition<E, A>> toGraph();

  Optional<Transition<E, A>> transition(State<E> state, Symbol<A> symbol);

  Set<Symbol<A>> symbols(State<E> state);
}
