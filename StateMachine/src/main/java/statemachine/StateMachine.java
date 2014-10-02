package statemachine;

import automata.Symbol;

public interface StateMachine {

  boolean acceptInput(Symbol... symbols);

}
