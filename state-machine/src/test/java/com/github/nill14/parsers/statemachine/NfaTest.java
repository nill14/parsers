package com.github.nill14.parsers.statemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.SortedSet;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.automaton.DFA;
import com.github.nill14.parsers.automaton.NFA;
import com.github.nill14.parsers.automaton.NFABuilder;
import com.github.nill14.parsers.automaton.impl.FAIndexer;
import com.github.nill14.parsers.automaton.impl.NFAImpl;
import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.google.common.collect.Sets;

@SuppressWarnings("nls")
public class NfaTest {

	private static final Logger log = LoggerFactory.getLogger(NfaTest.class);
	final StateMachineFactory<String, Integer> factory = StateMachineFactory.newFactory(
			String.class, Integer.class);
	
	@Test
	public void test() {

		NFABuilder<String, Integer> builder = factory.newNFABuilder();

		builder.addState("A");
		builder.addState("B");

		builder.addTransition("A", "A", 0);
		builder.addTransition("A", "A", 1);
		builder.addTransition("A", "B", 1);

		builder.addInputState("A");
		builder.addOutputState("B");

		NFA<String, Integer> nfa = builder.buildNFA();
		log.info("{}", nfa);
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		FAIndexer<String, Integer> indexer = new FAIndexer<>((NFAImpl) nfa);

		assertEquals(Sets.newHashSet("A"), indexer.transitions("A", 0));
		assertEquals(Sets.newHashSet("A", "B"), indexer.transitions("A", 1));
		assertEquals(Sets.newHashSet(), indexer.transitions("B", 1));

		DFA<String, Integer> dfa = builder.buildDFA();
		log.info("{}", dfa);
		dfaTester(dfa);
	}

	@Test
	public void nfaEpsilonTest() {
		NFABuilder<String, Integer> builder = factory.newNFABuilder();
		// see http://en.wikipedia.org/wiki/Powerset_construction example

		builder.addState("A");
		builder.addState("B");
		builder.addState("C");
		builder.addState("D");

		builder.addInputState("A");
		builder.addOutputState("C");
		builder.addOutputState("D");

		builder.addTransition("A", "B", 0);
		builder.addTransition("A", "C");
		builder.addTransition("B", "B", 1);
		builder.addTransition("B", "D", 1);
		builder.addTransition("C", "B");
		builder.addTransition("C", "D", 0);
		builder.addTransition("D", "C", 0);

		NFA<String, Integer> nfa = builder.buildNFA();
		log.info("{}", nfa);

		DFA<String, Integer> dfa = builder.buildDFA();
		log.info("{}", dfa);
		dfaTester(dfa);
	}

	@Test
	public void nfa2dfaTest1() {
		NFABuilder<String, Integer> builder = factory.newNFABuilder();

		builder.addInputState("A");
		builder.addOutputState("D");

		builder.addTransition("A", "A", 0);
		builder.addTransition("A", "A", 1);
		
		builder.addTransition("A", "B", 1);
		builder.addTransition("B", "C", 1);
		builder.addTransition("C", "D", 1);

		DFA<String, Integer> dfa = builder.buildDFA();
		log.info("{}", dfa);
		dfaTester(dfa);

		StateMachine<String, Integer> stateMachine = factory.newStateMachine(builder);
		
		testAccept(stateMachine, 0, 1, 1, 1);
		testAccept(stateMachine, 0, 1, 1, 0);
		testAccept(stateMachine, 0, 1, 1, 1, 1, 1);
		
	}

	@Test
	public void nfaEpsilon2Test() {
		NFABuilder<String, Integer> builder = factory.newNFABuilder();

		builder.addState("A");
		builder.addState("B");
		builder.addState("C");
		builder.addState("D");

		builder.addInputState("A");
		builder.addOutputState("D");

		builder.addTransition("A", "B");
		builder.addTransition("A", "C");
		builder.addTransition("B", "D");
                             
		builder.addTransition("A", "B", 0);
		builder.addTransition("B", "C", 0);
		builder.addTransition("C", "D", 1);

		NFA<String, Integer> nfa = builder.buildNFA();
		log.info("{}", nfa);

		DFA<String, Integer> dfa = builder.buildDFA();
		log.info("{}", dfa);
		dfaTester(dfa);

		assertTrue(dfa.outputStates().contains(dfa.initialState()));
	}


	private static void dfaTester(DFA<String, Integer> dfa) {
		Set<State<String>> states = dfa.states();
		SortedSet<Symbol<Integer>> symbols = dfa.alphabet();

		assertFalse(symbols.contains(Symbol.epsilon()));

		for (State<String> state : states) {
			for (Symbol<Integer> symbol : dfa.symbols(state)) {
				boolean accept = dfa.accept(state, symbol);
				assertTrue(String.format("%s %s", state, symbol), accept);
			}
		}
	}
	
	private static void testAccept(StateMachine<String, Integer> machine, Integer... input) {

		for (Integer symbol : input) {
			boolean accept = machine.accept(symbol);
			assertTrue(accept);
		}
	}

}
