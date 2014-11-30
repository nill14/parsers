package com.github.nill14.parsers.statemachine;

import com.github.nill14.parsers.automaton.model.Transition;

public interface TransitionListener {

	void changeState(StateMachine stateMachine, Transition transition);
	
}
