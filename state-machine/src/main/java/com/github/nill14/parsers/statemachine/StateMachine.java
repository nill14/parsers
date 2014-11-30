package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;

public interface StateMachine<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	void resetState();
	
	State<E> getState();

	boolean accept(A symbol);

	boolean isOutputState();

	SymbolChain<A> getSymbolChain();
	
	void addStateChangeListener(StateChangeListener listener);
	void addStateChangeListener(State state, StateChangeListener listener);

	void removeStateChangeListener(StateChangeListener listener);
	void removeStateChangeListener(State state, StateChangeListener listener);

	void addTransitionListener(TransitionListener listener);
	void addTransitionListener(Symbol input, TransitionListener listener);

	void removeTransitionListener(TransitionListener listener);
	void removeTransitionListener(Symbol input, TransitionListener listener);

	void addStateMachineListener(StateMachineListener listener);
	void removeStateMachineListener(StateMachineListener listener);
}
