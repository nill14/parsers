package com.github.nill14.parsers.automaton.impl;

import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.google.common.collect.Maps;

public class FAIndexer<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	private final AbstractAutomaton<E, A> automaton;
	private Map<E, State<E>> nodeIndex;
	private Map<A, Symbol<A>> symbolIndex;

	public FAIndexer(AbstractAutomaton<E, A> automaton) {
		this.automaton = automaton;
		
		nodeIndex = Maps.newHashMap();
		automaton.states().stream().forEach(s -> {
			s.elements().forEach(e -> nodeIndex.put(e, s));
		});
		
		symbolIndex = Maps.newHashMap();
		automaton.alphabet().stream()
			.filter(s -> !s.isEpsilon())
			.forEach(s -> symbolIndex.put(s.symbol(), s));
	}

	public State<E> getState(E state) {
		return nodeIndex.get(state);
	}
	
	public Symbol<A> getSymbol(A symbol) {
		return symbolIndex.get(symbol);
	}
	
	public boolean accept(E state, A symbol) {
		return automaton.accept(getState(state), getSymbol(symbol));
	}

//	public Set<State<Comparable<? super E>>> initialStates() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public Set<State<Comparable<? super E>>> outputStates() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public Set<E> states() {
		return automaton.states().stream().flatMap(s -> s.elements().stream()).collect(toSet());
	}

	public Set<A> alphabet() {
		return automaton.alphabet().stream().filter(a -> !a.isEpsilon()).map(s -> s.symbol()).collect(toSet());
	}


	public Set<E> transitions(E state, A symbol) {
		return automaton.transitions().apply(getState(state), getSymbol(symbol))
				.stream()
				.flatMap(t -> t.target().elements().stream())
				.collect(toSet());
	}

	public Set<A> symbols(E state) {
		return automaton.symbols(getState(state)).stream().map(s -> s.symbol()).collect(toSet());
	}
}
