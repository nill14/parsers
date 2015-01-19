package com.github.nill14.parsers.statemachine.osgi;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.statemachine.StateChangeListener;
import com.github.nill14.parsers.statemachine.StateMachine;
import com.google.common.base.Supplier;

public class StartingChangeListener implements StateChangeListener<OSGiLifecycle, OSGiOperation> {

	private Supplier<OSGiOperation> next;

	public StartingChangeListener(Supplier<OSGiOperation> next) {
		this.next = next;
	}
	
	@Override
	public OSGiOperation enterState(
			StateMachine<OSGiLifecycle, OSGiOperation> stateMachine,
			State<OSGiLifecycle> state) {
		
		if (state.elements().contains(OSGiLifecycle.STARTING)) {
			if (next.get() == OSGiOperation.FAILED) {
				return OSGiOperation.FAILED;
			} else {
				return OSGiOperation.STARTED;
			}
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
