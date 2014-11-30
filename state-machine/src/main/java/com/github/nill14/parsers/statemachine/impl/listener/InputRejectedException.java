package com.github.nill14.parsers.statemachine.impl.listener;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.statemachine.SymbolChain;

public class InputRejectedException extends RuntimeException {


	private static final long serialVersionUID = -1506103915097383043L;
	private final State<?> state;
	private final SymbolChain<?> symbols;


	public InputRejectedException(State<?> state, SymbolChain<?> symbols) {
		super(message(state, symbols));
		this.state = state;
		this.symbols = symbols;
	}


	private static final String message(State<?> state, SymbolChain<?> symbols) {
		return String.format("State=%s, symbols=%s", state, symbols);
	}


	public State<?> getState() {
		return state;
	}


	public SymbolChain<?> getSymbols() {
		return symbols;
	}
	
	
	
}
