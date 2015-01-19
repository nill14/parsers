package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.model.State;

public interface StateMachine<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	/**
	 * Resets the actual state of the state machine to input state and removes the current symbol chain.
	 * If the actual state is not output state then 
	 * {@link StateMachineListener#rejectInput(StateMachine, SymbolChain, State)} event is triggered.
	 */
	SymbolChain<A> resetInput();
	
	/**
	 * Similar to {@link #resetInput()} but the actual state is not affected.
	 * Consumes and resets the actual symbol chain. 
	 * If the actual state is not output state then 
	 * {@link StateMachineListener#rejectInput(StateMachine, SymbolChain, State)} event is triggered.
	 */
	SymbolChain<A> acceptInput();
	
	State<E> getState();

	boolean input(A symbol);

	boolean isOutputState();

	SymbolChain<A> getSymbolChain();
	
	void addStateChangeListener(StateChangeListener<E,A> listener);
	void addStateChangeListener(E state, StateChangeListener<E,A> listener);

	void removeStateChangeListener(StateChangeListener<E,A> listener);
	void removeStateChangeListener(E state, StateChangeListener<E,A> listener);

	void addTransitionListener(TransitionListener<E,A> listener);
	void addTransitionListener(A input, TransitionListener<E,A> listener);

	void removeTransitionListener(TransitionListener<E,A> listener);
	void removeTransitionListener(A input, TransitionListener<E,A> listener);

	void addStateMachineListener(StateMachineListener<E,A> listener);
	void removeStateMachineListener(StateMachineListener<E,A> listener);
}
