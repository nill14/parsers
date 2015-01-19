package com.github.nill14.parsers.automaton.model;

import com.google.common.base.Preconditions;

public class AlphabetSymbol<A extends Comparable<? super A>> implements Symbol<A> {

	public static <A extends Comparable<? super A>> Symbol<A> of(A symbol) {
		return new AlphabetSymbol<>(symbol);
	}

	private final A symbol;

	private AlphabetSymbol(A symbol) {
		Preconditions.checkNotNull(symbol);
		this.symbol = symbol;
	}

	@Override
	public A symbol() {
		return symbol;
	}
	
	@Override
	public boolean isEpsilon() {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		AlphabetSymbol other = (AlphabetSymbol) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return symbol.toString();
	}

	@Override
	public int compareTo(Symbol<A> o) {
		if (o.isEpsilon()) {
			return 1;
		}
		return symbol.compareTo(o.symbol());
	}
	
	
}
