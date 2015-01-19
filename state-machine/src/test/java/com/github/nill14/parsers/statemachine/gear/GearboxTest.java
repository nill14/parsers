package com.github.nill14.parsers.statemachine.gear;

import static com.github.nill14.parsers.statemachine.gear.Gearbox.*;
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

public class GearboxTest {
	
	private static final Logger log = LoggerFactory.getLogger(GearboxTest.class);
	private static StateMachineFactory<Integer,Gearbox> factory;
	private static NFABuilder<Integer,Gearbox> builder;
	private StateMachine<Integer,Gearbox> machine;

	@BeforeClass
	public static void setUp() {
		factory = StateMachineFactory.newFactory(Integer.class, Gearbox.class);
		
		builder = factory.newNFABuilder();
		builder.addInputState(1);
		builder.addOutputState(5);
		
		builder.addTransition(1, 2, GEAR_UP);
		builder.addTransition(2, 3, GEAR_UP);
		builder.addTransition(3, 4, GEAR_UP);
		builder.addTransition(4, 5, GEAR_UP);
		
		builder.addTransition(5, 4, GEAR_DOWN);
		builder.addTransition(4, 3, GEAR_DOWN);
		builder.addTransition(3, 2, GEAR_DOWN);
		builder.addTransition(2, 1, GEAR_DOWN);
	}
	
	//todo output state diff from input state
	//test transition redirect, cancel
	
	@Before
	public void init() {
		machine = factory.newStateMachine(builder);
	}
	
	@Test
	public void testBaseFlow() {
		log.info("testBaseFlow");
		testAccept(machine, GEAR_UP, GEAR_UP);
		
		machine.resetInput();
		testAccept(machine, GEAR_UP, GEAR_UP, GEAR_UP);
	}
	
	@Test
	public void testMachineListeners() {
		log.info("testMachineListeners");
		
		machine.addStateMachineListener(new InputRejectedListener<>());
		machine.addStateMachineListener(new InputAcceptedListener<>());
		
		testAccept(machine, GEAR_UP, GEAR_UP, GEAR_UP, GEAR_UP, GEAR_DOWN, GEAR_UP);
	}
	
	
	private static void testAccept(StateMachine<Integer, Gearbox> machine, Gearbox... input) {

		for (Gearbox symbol : input) {
			State<Integer> state = machine.getState();
			boolean accept = machine.input(symbol);
			log.debug("{}", machine.getSymbolChain());
			assertTrue(String.format("%s %s", state, symbol), accept);
		}
	}
	
	private static void testFail(StateMachine<Integer, Gearbox> machine, Gearbox... input) {

		for (Gearbox symbol : input) {
			State<Integer> state = machine.getState();
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
