package com.github.nill14.parsers.statemachine.impl;

import java.util.Optional;

import com.github.nill14.parsers.automaton.DFA;
import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.github.nill14.parsers.automaton.model.Transition;
import com.github.nill14.parsers.statemachine.StateChangeListener;
import com.github.nill14.parsers.statemachine.StateMachine;
import com.github.nill14.parsers.statemachine.StateMachineListener;
import com.github.nill14.parsers.statemachine.SymbolChain;
import com.github.nill14.parsers.statemachine.TransitionListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class StateMachineImpl<E extends Comparable<? super E>, A extends Comparable<? super A>> implements StateMachine<E, A> {

	private final DFA<E, A> automaton;
	private final StateChangeHelper<E, A> helper = new StateChangeHelper<>();
	private State<E> state;
	private SymbolChain<A> symbols = EmptySymbolChain.getInstance();
	private ImmutableMap<A, Symbol<A>> symbolIndex;

	public StateMachineImpl(DFA<E, A> automaton) {
		this.automaton = automaton;
		this.state = automaton.initialState();
		symbolIndex = Maps.uniqueIndex(automaton.alphabet(), a -> a.symbol());
	}

	@Override
	public boolean accept(A input) {
		symbols = ArraySymbolChain.newChain(symbols, input);
		Symbol<A> symbol = symbolIndex.get(input);
		Preconditions.checkNotNull(symbol);

		Optional<Transition<E, A>> opt = automaton.transition(state, symbol);
		if (opt.isPresent()) {
			Transition<E, A> transition = opt.get();
			helper.changeState(this, transition, symbols);
			state = transition.target();
			return true; 
		} else {
			resetState();
			return false;
		} 
	}

	@Override
	public void resetState() {
		helper.resetState(this, symbols, state);
		
		state = automaton.initialState();
		symbols = EmptySymbolChain.getInstance();
	}
	
	@Override
	public State<E> getState() {
		return state;
	}

	@Override
	public boolean isOutputState() {
		return automaton.outputStates().contains(state);
	}
	
	
	@Override
	public SymbolChain<A> getSymbolChain() {
		return symbols;
	}
	
	@Override
	public void addStateChangeListener(StateChangeListener listener) {
		helper.addStateChangeListener(listener);
	}

	@Override
	public void addStateChangeListener(State state, StateChangeListener listener) {
		helper.addStateChangeListener(state, listener);
	}

	@Override
	public void removeStateChangeListener(StateChangeListener listener) {
		helper.removeStateChangeListener(listener);
	}

	@Override
	public void removeStateChangeListener(State state,
			StateChangeListener listener) {
		helper.removeStateChangeListener(state, listener);
	}

	@Override
	public void addTransitionListener(TransitionListener listener) {
		helper.addTransitionListener(listener);
	}

	@Override
	public void addTransitionListener(Symbol input, TransitionListener listener) {
		helper.addTransitionListener(input, listener);
	}

	@Override
	public void removeTransitionListener(TransitionListener listener) {
		helper.removeTransitionListener(listener);
	}

	@Override
	public void removeTransitionListener(Symbol input, TransitionListener listener) {
		helper.removeTransitionListener(input, listener);
	}

	@Override
	public void addStateMachineListener(StateMachineListener listener) {
		helper.addStateMachineListener(listener);
	}

	@Override
	public void removeStateMachineListener(StateMachineListener listener) {
		helper.removeStateMachineListener(listener);
	}
}
