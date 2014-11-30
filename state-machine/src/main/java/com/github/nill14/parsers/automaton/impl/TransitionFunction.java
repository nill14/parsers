package com.github.nill14.parsers.automaton.impl;

import java.util.Set;
import java.util.SortedSet;
import java.util.function.BiFunction;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.github.nill14.parsers.automaton.model.Transition;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;

public class TransitionFunction<E extends Comparable<? super E>, A extends Comparable<? super A>> implements BiFunction<State<E>, Symbol<A>, Set<Transition<E, A>>> {

	private final ImmutableSetMultimap<KeyPair<E, A>, Transition<E, A>> map;
	private final ImmutableSet<Transition<E, A>> transitions;
	private final ImmutableSortedSet<Symbol<A>> alphabet;
	private final ImmutableSetMultimap<State<E>, Symbol<A>> symbols;

	private TransitionFunction(Builder<E, A> builder) {
		map = builder.mapBuilder.build();
		transitions = builder.transitions.build();
		alphabet = ImmutableSortedSet.copyOf(transitions.stream()
				.map(t -> t.symbol()).iterator());

		ImmutableSetMultimap.Builder<State<E>, Symbol<A>> stateSymbols = ImmutableSetMultimap.builder();
		transitions.forEach(t -> stateSymbols.put(t.source(), t.symbol()));
		this.symbols = stateSymbols.build();
	}

	@Override
	public Set<Transition<E, A>> apply(State<E> state, Symbol<A> symbol) {
		return map.get(new KeyPair<>(state, symbol));
	}

	public Set<Transition<E, A>> transitions() {
		return transitions;
	}

	public Set<Symbol<A>> symbols(State<E> state) {
		return symbols.get(state);
	}

	public SortedSet<Symbol<A>> symbols() {
		return alphabet;
	}

	@Override
	public String toString() {
		return transitions.toString();
	}

	public static class Builder<E extends Comparable<? super E>, A extends Comparable<? super A>> {
		private ImmutableSetMultimap.Builder<KeyPair<E, A>, Transition<E, A>> mapBuilder = ImmutableSetMultimap.builder();
		private ImmutableSet.Builder<Transition<E, A>> transitions = ImmutableSet.builder();

		public Builder<E, A> add(Transition<E, A> transition) {
			KeyPair<E, A> key = new KeyPair<>(transition.source(), transition.symbol());
			mapBuilder.put(key, transition);
			transitions.add(transition);
			return this;
		}

		public TransitionFunction<E, A> build() {
			return new TransitionFunction<E, A>(this);
		}
	}

	public static <E extends Comparable<? super E>, A extends Comparable<? super A>> Builder<E, A> builder() {
		return new Builder<>();
	}

	private static class KeyPair<E extends Comparable<? super E>, A extends Comparable<? super A>> {

		final State<E> state;
		final Symbol<A> symbol;

		private KeyPair(State<E> state, Symbol<A> symbol) {
			this.state = state;
			this.symbol = symbol;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((state == null) ? 0 : state.hashCode());
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
			KeyPair other = (KeyPair) obj;
			if (state == null) {
				if (other.state != null)
					return false;
			} else if (!state.equals(other.state))
				return false;
			if (symbol == null) {
				if (other.symbol != null)
					return false;
			} else if (!symbol.equals(other.symbol))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.format("StateSymbolPair [state=%s, symbol=%s]", state, symbol);
		}
	}

}
