package com.github.nill14.parsers.automaton.impl;

import java.util.Map;

import com.github.nill14.parsers.automaton.DFA;
import com.github.nill14.parsers.automaton.NFA;
import com.github.nill14.parsers.automaton.NFABuilder;
import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public final class NFABuilderImpl<E extends Comparable<? super E>, A extends Comparable<? super A>> implements NFABuilder<E, A> {

	final AutomatonBuilder<E, A> builder = new AutomatonBuilder<>();
	final Map<E, State<E>> nodes = Maps.newHashMap();
	final Map<A, Symbol<A>> symbols = Maps.newHashMap();

	public NFABuilderImpl() {}

	private State<E> stateFor(E state) {
		Preconditions.checkNotNull(state);	
		return nodes.computeIfAbsent(state, State::of);
	}

	private Symbol<A> symbolFor(A symbol) {
		Preconditions.checkNotNull(symbol);
		return symbols.computeIfAbsent(symbol, Symbol::of);
	}
	
	@Override
	public NFABuilder<E, A> addState(E state) {
		builder.addState(stateFor(state));
		return this;
	}
	
	@Override
	public NFABuilder<E, A> addTransition(E source, E target, A input) {
		builder.addTransition(stateFor(source), stateFor(target), symbolFor(input));
		return this;
	}

	@Override
	public NFABuilder<E, A> addTransition(E source, E target) {
		builder.addTransition(stateFor(source), stateFor(target), Symbol.epsilon());
		return this;
	}
	
	@Override
	public NFABuilder<E, A> addInputState(E inputNode) {
		builder.addInputState(stateFor(inputNode));
		return this;
	}

	@Override
	public NFABuilder<E, A> addOutputState(E outputNode) {
		builder.addOutputState(stateFor(outputNode));
		return this;
	}

	@Override
	public NFA<E, A> buildNFA() {
		return builder.buildNFA();
	}

	@Override
	public DFA<E, A> buildDFA() {
		return new PowersetHelper<>(builder.buildNFA()).toDFA();
	}

}