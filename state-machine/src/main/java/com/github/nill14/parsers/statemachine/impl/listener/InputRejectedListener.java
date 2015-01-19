package com.github.nill14.parsers.statemachine.impl.listener;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.statemachine.StateMachine;
import com.github.nill14.parsers.statemachine.StateMachineListener;
import com.github.nill14.parsers.statemachine.SymbolChain;

public class InputRejectedListener<E extends Comparable<? super E>, A extends Comparable<? super A>>  implements StateMachineListener<E, A>{

	@Override
	public void acceptInput(StateMachine<E, A> stateMachine,
			SymbolChain<A> symbols, State<E> finalState) {	}

	@Override
	public void rejectInput(StateMachine<E, A> stateMachine,
			SymbolChain<A> symbols, State<E> actualState) {

		/*
		 * Another option is to follow the return value of 
		 * {@link StateMachine#input(symbol)}
		 */
		throw new InputRejectedException(actualState, symbols);
	}

}
