package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.model.State;

public interface StateMachineListener<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	void acceptInput(StateMachine<E, A> stateMachine, SymbolChain<A> symbols, State<E> finalState);
	
	/**
	 * Called when an input is rejected
	 * @param stateMachine
	 * @param symbols The symbol chain including rejected symbol
	 * @param finalState The last valid state
	 */
	void rejectInput(StateMachine<E, A> stateMachine, SymbolChain<A> symbols, State<E> finalState);
}
