package com.github.nill14.parsers.statemachine.impl;

import java.util.Iterator;

import com.github.nill14.parsers.statemachine.SymbolChain;
import com.google.common.collect.ImmutableList;

final class EmptySymbolChain<A> implements SymbolChain<A> {
	
	private static final EmptySymbolChain<?> instance = new EmptySymbolChain<>();
	@SuppressWarnings("unchecked")
	public static <A> EmptySymbolChain<A> getInstance() {
		return (EmptySymbolChain<A>) instance;
	}
	
	private EmptySymbolChain() {
	}

	@Override
	public Iterator<A> iterator() {
		return ImmutableList.<A>of().iterator();
	}

	@Override
	public A getFirst() {
		throw new UnsupportedOperationException("Empty");
	}

	@Override
	public A getLast() {
		throw new UnsupportedOperationException("Empty");
	}

	@Override
	public SymbolChain<A> stripLast() {
		return this;
	}
	
	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public String toString() {
		return String.format("SymbolChain [ε]");
	}
	
	
}