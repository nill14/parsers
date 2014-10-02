package automata;

import java.util.Set;
import java.util.SortedSet;

public interface FiniteAutomaton {

  boolean accept(State state, Symbol input);
  
  State initialState();
  
  Set<State> outputStates();
  
  Set<State> states();
  
  SortedSet<Symbol> alphabet();
}
