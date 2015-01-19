package com.github.nill14.parsers.statemachine.osgi;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.statemachine.StateChangeListener;
import com.github.nill14.parsers.statemachine.StateMachine;

public class StoppingChangeListener implements StateChangeListener<OSGiLifecycle, OSGiOperation> {

	
	@Override
	public OSGiOperation enterState(
			StateMachine<OSGiLifecycle, OSGiOperation> stateMachine,
			State<OSGiLifecycle> state) {
		
		if (state.elements().contains(OSGiLifecycle.STOPPING)) {
			return OSGiOperation.STOPPED;
		}
		
		return null;
	}

	@Override
	public void exitState(
			StateMachine<OSGiLifecycle, OSGiOperation> stateMachine,
			State<OSGiLifecycle> state) {
		// TODO Auto-generated method stub
		
	}


}
