package com.github.nill14.parsers.automaton.model;


public interface Symbol<A extends Comparable<? super A>> extends Comparable<Symbol<A>> {

	A symbol();

	boolean isEpsilon();

	public static <A extends Comparable<? super A>> Symbol<A> epsilon() {
		return EpsilonSymbol.<A> epsilon();
	}

	public static <A extends Comparable<? super A>> Symbol<A> of(A symbol) {
		return AlphabetSymbol.of(symbol);
	}

}
