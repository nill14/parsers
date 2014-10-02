package automata;

import java.util.Optional;


public interface DFA extends FiniteAutomaton {

  Optional<State> transition(State node, Symbol input);
  
}
