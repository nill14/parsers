package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.model.Transition;

public interface TransitionListener<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	/**
	 * 
	 * @param stateMachine
	 * @param transition
	 * @return The modified symbol or null if the current transition shall proceed
	 */
	A changeState(StateMachine<E, A> stateMachine, Transition<E, A> transition);
	
}
