package com.github.nill14.parsers.automaton.impl;

import java.util.Set;

import com.github.nill14.parsers.automaton.DFA;
import com.github.nill14.parsers.automaton.NFA;
import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.github.nill14.parsers.automaton.model.Transition;
import com.google.common.collect.Sets;

public class AutomatonBuilder<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	final Set<State<E>> nodes = Sets.newHashSet();
	final Set<Symbol<A>> symbols = Sets.newHashSet();
	final Set<State<E>> inputNodes = Sets.newHashSet();
	final Set<State<E>> outputNodes = Sets.newHashSet();
	final TransitionFunction.Builder<E, A> transitions = TransitionFunction.builder();

	public AutomatonBuilder() {}

	public AutomatonBuilder<E, A> addState(State<E> state) {
		nodes.add(state);
		return this;
	}
	
	public AutomatonBuilder<E, A> addTransition(State<E> source, State<E> target, Symbol<A> input) {
		nodes.add(source);
		nodes.add(target);
		transitions.add(new Transition<>(source, target, input));
		return this;
	}

	public AutomatonBuilder<E, A> addInputState(State<E> inputNode) {
		nodes.add(inputNode);
		inputNodes.add(inputNode);
		return this;
	}

	public AutomatonBuilder<E, A> addOutputState(State<E> outputNode) {
		nodes.add(outputNode);
		outputNodes.add(outputNode);
		return this;
	}

	public NFA<E, A> buildNFA() {
		return new NFAImpl<>(this);
	}

	public DFA<E, A> buildDFA() {
		return new DFAImpl<>(this);
	}

}