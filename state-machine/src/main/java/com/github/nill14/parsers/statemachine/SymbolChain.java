package com.github.nill14.parsers.statemachine;


public interface SymbolChain<A> extends Iterable<A> {
	
	A getFirst();
	A getLast();
	
	SymbolChain<A> stripLast();
	
	int size();
	boolean isEmpty();
}
