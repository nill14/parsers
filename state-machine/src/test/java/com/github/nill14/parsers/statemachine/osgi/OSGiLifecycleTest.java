package com.github.nill14.parsers.statemachine.osgi;

import static com.github.nill14.parsers.statemachine.osgi.OSGiLifecycle.*;
import static com.github.nill14.parsers.statemachine.osgi.OSGiOperation.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.automaton.NFABuilder;
import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.statemachine.StateMachine;
import com.github.nill14.parsers.statemachine.StateMachineFactory;
import com.github.nill14.parsers.statemachine.impl.listener.InputAcceptedListener;
import com.github.nill14.parsers.statemachine.impl.listener.InputRejectedListener;

public class OSGiLifecycleTest {
	
	private static final Logger log = LoggerFactory.getLogger(OSGiLifecycleTest.class);
	private static StateMachineFactory<OSGiLifecycle,OSGiOperation> factory;
	private static NFABuilder<OSGiLifecycle, OSGiOperation> builder;
	private StateMachine<OSGiLifecycle, OSGiOperation> machine;

	@BeforeClass
	public static void setUp() {
		factory = StateMachineFactory.newFactory(OSGiLifecycle.class, OSGiOperation.class);
		
		builder = factory.newNFABuilder();
		builder.addInputState(UNINSTALLED);
		builder.addOutputState(UNINSTALLED);
		builder.addTransition(UNINSTALLED, INSTALLED, INSTALL);
		builder.addTransition(INSTALLED, RESOLVED, RESOLVE);
		builder.addTransition(RESOLVED, INSTALLED, UPDATE);
		
		builder.addTransition(RESOLVED, STARTING, START);
		builder.addTransition(STARTING, ACTIVE, STARTED);
		builder.addTransition(ACTIVE, STOPPING, STOP);
		builder.addTransition(STOPPING, RESOLVED, STOPPED);
		
		builder.addTransition(INSTALLED, UNINSTALLED, UNINSTALL);
		builder.addTransition(RESOLVED, UNINSTALLED, UNINSTALL);
		builder.addTransition(INSTALLED, INSTALLED, UPDATE);
		builder.addTransition(STARTING, RESOLVED, FAILED);
		
	}
	
	@Before
	public void init() {
		machine = factory.newStateMachine(builder);
	}
	
	@Test
	public void testBaseFlow() {
		log.info("testBaseFlow");
		testAccept(machine, INSTALL, RESOLVE, START, STARTED, STOP, STOPPED, UPDATE, UNINSTALL);
		
		machine.resetInput();
		testAccept(machine, INSTALL, RESOLVE, UNINSTALL);
	}
	
	@Test
	public void testStartingWithoutListeners() {
		log.info("testStartingWithoutListeners");
		testFail(machine, INSTALL, RESOLVE, START, STOP, UPDATE, UNINSTALL);
	}
	
	@Test
	public void testStarting() {
		log.info("testStarting");
		machine.addStateChangeListener(STARTING, new StartingChangeListener(() -> STARTED));
		machine.addStateChangeListener(STOPPING, new StoppingChangeListener());
		
		testAccept(machine, INSTALL, RESOLVE, START, STOP, UPDATE, UNINSTALL);
	}
	
	@Test
	public void testWithMachineListeners() {
		log.info("testWithMachineListeners");
		machine.addStateChangeListener(STARTING, new StartingChangeListener(() -> STARTED));
		machine.addStateChangeListener(STOPPING, new StoppingChangeListener());
		
		machine.addStateMachineListener(new InputRejectedListener<>());
		machine.addStateMachineListener(new InputAcceptedListener<>());
		
		testAccept(machine, INSTALL, RESOLVE, START, STOP, UPDATE, UNINSTALL, INSTALL);
	}
	
	
	private static void testAccept(StateMachine<OSGiLifecycle, OSGiOperation> machine, OSGiOperation... input) {

		for (OSGiOperation symbol : input) {
			State<OSGiLifecycle> state = machine.getState();
			boolean accept = machine.input(symbol);
			log.debug("{}", machine.getSymbolChain());
			assertTrue(String.format("%s %s", state, symbol), accept);
		}
	}
	
	private static void testFail(StateMachine<OSGiLifecycle, OSGiOperation> machine, OSGiOperation... input) {

		for (OSGiOperation symbol : input) {
			State<OSGiLifecycle> state = machine.getState();
			boolean accept = machine.input(symbol);
			log.debug("{}", machine.getSymbolChain());
			if (!accept) {
				assertFalse(accept);
				return;
			}
		}
		assertFalse("Expected fail: " + machine.getSymbolChain(), true);
	}
	
}
