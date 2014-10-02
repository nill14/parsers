package statemachine.fa.impl;

import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;
import java.util.Set;

import statemachine.StateMachine;
import automata.DFA;
import automata.FiniteAutomaton;
import automata.State;
import automata.Symbol;

import com.google.common.collect.Queues;

public class StateMachineImpl implements StateMachine {

  private DFA dfa;

  public StateMachineImpl(DFA dfa) {
    this.dfa = dfa;
  }

  @Override
  public boolean acceptInput(Symbol... symbols) {
    Deque<Symbol> deque = Queues.newArrayDeque(Arrays.asList(symbols));
    
    State state = dfa.initialState();
    while (!deque.isEmpty()) {
      Symbol symbol = deque.poll();
      Optional<State> transition = dfa.transition(state, symbol);
      if (transition.isPresent()) {
        state = transition.get();
      } else {
        return false;
      }
    }
    
    return dfa.outputStates().contains(state);
  }

}
