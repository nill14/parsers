package com.github.nill14.parsers.statemachine.impl.listener;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.statemachine.StateMachine;
import com.github.nill14.parsers.statemachine.StateMachineListener;
import com.github.nill14.parsers.statemachine.SymbolChain;

public class InputAcceptedListener<E extends Comparable<? super E>, A extends Comparable<? super A>>  implements StateMachineListener<E, A>{

	@Override
	public void acceptInput(StateMachine<E, A> stateMachine,
			SymbolChain<A> symbols, State<E> finalState) {
		
		//suppose we recognize expression .*ing
		//then possible words might be ing, being, etc.
		//this listener simply accepts always the first possible input
		stateMachine.acceptInput();
	}

	@Override
	public void rejectInput(StateMachine<E, A> stateMachine,
			SymbolChain<A> symbols, State<E> actualState) {	}

}
