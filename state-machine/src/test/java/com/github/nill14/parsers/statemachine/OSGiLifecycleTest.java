package com.github.nill14.parsers.statemachine;

import static com.github.nill14.parsers.statemachine.OSGiLifecycle.ACTIVE;
import static com.github.nill14.parsers.statemachine.OSGiLifecycle.INSTALLED;
import static com.github.nill14.parsers.statemachine.OSGiLifecycle.RESOLVED;
import static com.github.nill14.parsers.statemachine.OSGiLifecycle.STARTING;
import static com.github.nill14.parsers.statemachine.OSGiLifecycle.STOPPING;
import static com.github.nill14.parsers.statemachine.OSGiLifecycle.UNINSTALLED;
import static com.github.nill14.parsers.statemachine.OSGiOperation.INSTALL;
import static com.github.nill14.parsers.statemachine.OSGiOperation.RESOLVE;
import static com.github.nill14.parsers.statemachine.OSGiOperation.START;
import static com.github.nill14.parsers.statemachine.OSGiOperation.STARTED;
import static com.github.nill14.parsers.statemachine.OSGiOperation.STOP;
import static com.github.nill14.parsers.statemachine.OSGiOperation.STOPPED;
import static com.github.nill14.parsers.statemachine.OSGiOperation.UNINSTALL;
import static com.github.nill14.parsers.statemachine.OSGiOperation.UPDATE;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.automaton.NFABuilder;
import com.github.nill14.parsers.automaton.model.State;

public class OSGiLifecycleTest {
	
	private static final Logger log = LoggerFactory.getLogger(OSGiLifecycleTest.class);

	@Test
	public void test1() {
		StateMachineFactory<OSGiLifecycle,OSGiOperation> factory = StateMachineFactory.newFactory(OSGiLifecycle.class, OSGiOperation.class);
		
		NFABuilder<OSGiLifecycle, OSGiOperation> builder = factory.newNFABuilder();
		builder.addInputState(UNINSTALLED);
		builder.addTransition(UNINSTALLED, INSTALLED, INSTALL);
		builder.addTransition(INSTALLED, RESOLVED, RESOLVE);
		builder.addTransition(RESOLVED, INSTALLED, UPDATE);
		
		builder.addTransition(RESOLVED, STARTING, START);
		builder.addTransition(STARTING, ACTIVE, STARTED);
		builder.addTransition(ACTIVE, STOPPING, STOP);
		builder.addTransition(STOPPING, RESOLVED, STOPPED);
		
		builder.addTransition(INSTALLED, UNINSTALLED, UNINSTALL);
		
		StateMachine<OSGiLifecycle, OSGiOperation> machine = factory.newStateMachine(builder);
		
		testAccept(machine, INSTALL, RESOLVE, START, STARTED, STOP, STOPPED, UPDATE, UNINSTALL);
	}
	
	
	private static void testAccept(StateMachine<OSGiLifecycle, OSGiOperation> machine, OSGiOperation... input) {

		for (OSGiOperation symbol : input) {
			State<OSGiLifecycle> state = machine.getState();
			boolean accept = machine.accept(symbol);
			log.debug("{}", machine.getSymbolChain());
			assertTrue(String.format("%s %s", state, symbol), accept);
		}
	}
	
}
