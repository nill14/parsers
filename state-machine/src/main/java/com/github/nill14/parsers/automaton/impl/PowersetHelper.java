package com.github.nill14.parsers.automaton.impl;

import static java.util.stream.Collectors.*;

import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import com.github.nill14.parsers.automaton.DFA;
import com.github.nill14.parsers.automaton.NFA;
import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PowersetHelper<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	private final AutomatonBuilder<E, A> builder = new AutomatonBuilder<>();
	private final NFA<E, A> nfa;
	private final Set<State<E>> markedStates = Sets.newHashSet();
	private final Map<State<E>, Set<State<E>>> stateMapping = Maps.newHashMap();

	public PowersetHelper(NFA<E, A> nfa) {
		this.nfa = nfa;
	}

	public DFA<E, A> toDFA() {

		State<E> initialState = initialState(nfa.initialStates());
		builder.addInputState(initialState);

		Deque<State<E>> processingQueue = Lists.newLinkedList();
		processingQueue.add(initialState);

		while (!processingQueue.isEmpty()) {

			State<E> sourceState = processingQueue.poll();
			if (!isMarkedDone(sourceState)) {

				Stream<Symbol<A>> symbols = symbols(sourceState);

				symbols.forEach(symbol -> {
					State<E> targetState = targetState(sourceState, symbol);
					builder.addTransition(sourceState, targetState, symbol);
					processingQueue.add(targetState);
				});

				markDone(sourceState);
			}
		}

		markedStates.stream().filter(this::isOutputState).forEach(builder::addOutputState);

		return builder.buildDFA();
	}

	private State<E> initialState(Set<State<E>> initialNfaStates) {
		return newCompoundState(epsilonClosure(initialNfaStates));
	}

	private State<E> targetState(State<E> dfaState, Symbol<A> symbol) {
		Set<State<E>> nfaStates = dfaMove(dfaState, symbol).collect(toSet());
		return newCompoundState(epsilonClosure(nfaStates));
	}
	
	private Stream<State<E>> dfaMove(State<E> dfaState, Symbol<A> symbol) {
		return stateMapping.get(dfaState).stream().flatMap(s -> nfaMove(s, symbol));
	}
	
	private Stream<State<E>> nfaMove(State<E> state, Symbol<A> symbol) {
		return nfa.transitions(state, symbol).stream().map(t -> t.target());
	}

	/**
	 * Closure is defined as set of all states reachable by epsilon move
	 * @param nfaState
	 * @return an epsilon closure of the state
	 */
	private ImmutableSet<State<E>> epsilonClosure(Set<State<E>> nfaStates) {
		Queue<State<E>> queue = Lists.newLinkedList(nfaStates);
		Set<State<E>> closure = Sets.newHashSet();
		while (!queue.isEmpty()) {
			State<E> nfaState = queue.poll();
			if (!closure.contains(nfaState)) {
				Stream<State<E>> stream = nfaMove(nfaState, Symbol.epsilon());
				stream.forEach(s -> queue.add(s));
				closure.add(nfaState);
			}
		}
		return ImmutableSet.copyOf(closure);
	}

	private State<E> newCompoundState(ImmutableSet<State<E>> nfaStates) {
		State<E> dfaState = State.from(nfaStates);
		stateMapping.put(dfaState, nfaStates);
		return dfaState;
	}

	private Stream<Symbol<A>> symbols(State<E> dfaState) {
		return stateMapping.get(dfaState).stream()
				.flatMap(s -> nfa.symbols(s).stream())
				.filter(s -> !s.isEpsilon())
				.distinct();
	}

	private void markDone(State<E> dfaState) {
		markedStates.add(dfaState);
	}

	private boolean isMarkedDone(State<E> dfaState) {
		return markedStates.contains(dfaState);
	}
	


	private boolean isOutputState(State<E> dfaState) {
		return stateMapping.get(dfaState).stream().anyMatch(s -> nfa.outputStates().contains(s));
	}

}
