package com.github.nill14.parsers.automaton;



public interface NFABuilder<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	NFABuilder<E, A> addState(E state);

	NFABuilder<E, A> addTransition(E source, E target, A symbol);

	/**
	 * Add new epsilon transition
	 * @param source
	 * @param target
	 * @return self
	 */
	NFABuilder<E, A> addTransition(E source, E target);
	
	NFABuilder<E, A> addInputState(E inputNode);

	NFABuilder<E, A> addOutputState(E outputNode);

	NFA<E, A> buildNFA();

	DFA<E, A> buildDFA();
}