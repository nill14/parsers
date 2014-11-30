package com.github.nill14.parsers.automaton;

import java.util.Set;
import java.util.SortedSet;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.github.nill14.parsers.automaton.model.Transition;
import com.github.nill14.parsers.graph.DirectedGraph;

public interface NFA<E extends Comparable<? super E>, A extends Comparable<? super A>> {

  boolean accept(State<E> state, Symbol<A> symbol);
  
  Set<State<E>> initialStates();
  
  Set<State<E>> outputStates();
  
  Set<State<E>> states();
  
  SortedSet<Symbol<A>> alphabet();
  
  DirectedGraph<State<E>, Transition<E, A>> toGraph();

  Set<Transition<E, A>> transitions(State<E> state, Symbol<A> symbol);

  Set<Symbol<A>> symbols(State<E> state);
}
