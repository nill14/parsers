package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.model.State;

public interface StateChangeListener<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	/**
	 * 
	 * @param stateMachine
	 * @param state
	 * @return the follow-up symbol or null if the state should not be modified
	 */
	A enterState(StateMachine<E, A> stateMachine, State<E> state);

	void exitState(StateMachine<E, A> stateMachine, State<E> state);

	
	
}
