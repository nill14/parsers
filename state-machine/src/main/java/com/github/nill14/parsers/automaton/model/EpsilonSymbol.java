package com.github.nill14.parsers.automaton.model;


public class EpsilonSymbol<A extends Comparable<? super A>> implements Symbol<A> {

	private static final EpsilonSymbol<?> INSTANCE = new EpsilonSymbol<>();

	@SuppressWarnings("unchecked")
	public static <A extends Comparable<? super A>> Symbol<A> epsilon() {
		return (Symbol<A>) INSTANCE;
	}

	private EpsilonSymbol() {
	}

	@Override
	public A symbol() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "ε";
	}

	@Override
	public boolean isEpsilon() {
		return true;
	}

	@Override
	public int compareTo(Symbol<A> o) {
		if (o.isEpsilon()) {
			return 0;
		} else {
			return -1;
		}
	}

}
