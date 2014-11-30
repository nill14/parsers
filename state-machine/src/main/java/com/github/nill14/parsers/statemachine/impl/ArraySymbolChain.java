package com.github.nill14.parsers.statemachine.impl;

import java.util.Iterator;

import com.github.nill14.parsers.statemachine.SymbolChain;
import com.google.common.collect.ImmutableList;

public class ArraySymbolChain<A> implements SymbolChain<A> {

	public static <A> SymbolChain<A> newChain(SymbolChain<A> oldChain, A next) {
		if (oldChain.isEmpty()) {
			return new ArraySymbolChain<>(next);
		} else {
			return new ArraySymbolChain<>((ArraySymbolChain<A>) oldChain, next);
		}
	}
	
	
	private final ImmutableList<A> list;
	private final A first;
	private final A last;
	private final SymbolChain<A> chain;
	
	public ArraySymbolChain(ArraySymbolChain<A> chain, A next) {
		this.chain = chain;
		list = ImmutableList.<A>builder()
				.addAll(chain.list)
				.add(next)
				.build();
		this.first = chain.first;
		this.last = next;
	}

	public ArraySymbolChain(A next) {
		this.chain = EmptySymbolChain.getInstance();
		list = ImmutableList.of(next);
		this.first = this.last = next;
	}
	
	@Override
	public Iterator<A> iterator() {
		return list.iterator();
	}

	@Override
	public A getFirst() {
		return first;
	}

	@Override
	public A getLast() {
		return last;
	}

	@Override
	public SymbolChain<A> stripLast() {
		return chain;
	}

	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public String toString() {
		return String.format("SymbolChain %s", list);
	}
	
	
}
