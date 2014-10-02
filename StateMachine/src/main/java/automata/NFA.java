package automata;

import java.util.Set;


public interface NFA extends FiniteAutomaton {


  Set<State> transitions(State state, Symbol input);
  
  DFA toDFA();
  
}
