package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.model.State;

public interface StateChangeListener {

	void enterState(StateMachine stateMachine, State state);
	
	void exitState(StateMachine stateMachine, State state);
	
}
