package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.model.State;

public interface StateMachineListener {

	void acceptInput(StateMachine stateMachine, SymbolChain symbols, State finalState);
	
	void rejectInput(StateMachine stateMachine, SymbolChain symbols, State actualState);
}
