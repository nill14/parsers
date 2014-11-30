package com.github.nill14.parsers.automaton.model;

import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;

public final class State<E extends Comparable<? super E>> {

	public static <E extends Comparable<? super E>> State<E> of(E element) {
		return new State<>(element);
	}

	public static <E extends Comparable<? super E>> State<E> from(Set<State<E>> states) {
		return new State<>(states);
	}

	private final ImmutableSortedSet<E> elements;
	private final E element;


	private State(Set<State<E>> states) {
		Preconditions.checkArgument(!states.isEmpty());
		Stream<E> stream = states.stream().flatMap(s -> s.elements().stream());
		this.elements = ImmutableSortedSet.copyOf(stream.iterator());
		this.element = elements.iterator().next();
	}

	private State(E element) {
		Preconditions.checkNotNull(element);
		this.element = element;
		this.elements = ImmutableSortedSet.of(element);
	}

	public String toString() {
		return elements.toString();
	}

	public SortedSet<E> elements() {
		return elements;
	}

	public E getFirst() {
		return element;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((elements == null) ? 0 : elements.hashCode());
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
		State other = (State) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}

}
