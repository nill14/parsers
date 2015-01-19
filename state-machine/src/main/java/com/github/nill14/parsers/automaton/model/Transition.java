package com.github.nill14.parsers.automaton.model;

import com.github.nill14.parsers.graph.GraphEdge;

public class Transition<E extends Comparable<? super E>, A extends Comparable<? super A>> implements GraphEdge<State<E>> {

	private final State<E> target;
	private final State<E> source;
	private final Symbol<A> symbol;

	public Transition(State<E> source, State<E> target, Symbol<A> symbol) {
		this.source = source;
		this.target = target;
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s) -> %s", source, symbol, target);
	}

	public boolean accept(Symbol<A> symbol) { //TODO checkme
		return this.symbol.equals(symbol);
	}

	@Override
	public State<E> target() {
		return target;
	}

	@Override
	public State<E> source() {
		return source;
	}

	public Symbol<A> symbol() {
		return symbol;
	}
}
