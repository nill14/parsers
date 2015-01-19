package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.NFABuilder;
import com.github.nill14.parsers.automaton.impl.NFABuilderImpl;
import com.github.nill14.parsers.statemachine.impl.StateMachineImpl;

public class StateMachineFactory<E extends Comparable<? super E>, A extends Comparable<? super A>> {
	
	public static <E extends Comparable<? super E>, A extends Comparable<? super A>> 
		StateMachineFactory<E, A> newFactory(Class<E> stateClass, Class<A> symbolClass) {
		
		return new StateMachineFactory<>();
	}
	
	private StateMachineFactory() {	}
	
	
	public NFABuilder<E, A> newNFABuilder() {
		return new NFABuilderImpl<>();
	}
	
	public StateMachine<E, A> newStateMachine(NFABuilder<E, A> nfaBuilder) {
		return new StateMachineImpl<>(nfaBuilder.buildDFA());
	}
}
