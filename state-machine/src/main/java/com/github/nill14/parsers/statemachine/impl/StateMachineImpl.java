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
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;

public class StateMachineImpl<E extends Comparable<? super E>, A extends Comparable<? super A>> implements StateMachine<E, A> {

	private final DFA<E, A> automaton;
	private final ImmutableMap<A, Symbol<A>> symbolIndex;
	private final ImmutableSetMultimap<E, State<E>> stateIndex;
	private final StateChangeHelper<E, A> helper = new StateChangeHelper<>();

	private State<E> state;
	private SymbolChain<A> symbols;

	public StateMachineImpl(DFA<E, A> automaton) {
		this.automaton = automaton;
		this.symbolIndex = Maps.uniqueIndex(automaton.alphabet(), a -> a.symbol());
		
		ImmutableSetMultimap.Builder<E, State<E>> stateBuilder = ImmutableSetMultimap.builder();
		automaton.states().stream().forEach(s -> {
			s.elements().forEach(e -> stateBuilder.put(e, s));
		});
		this.stateIndex = stateBuilder.build();
		this.state = automaton.initialState();
		this.symbols = EmptySymbolChain.getInstance();
	}

	@Override
	public boolean input(A input) {
		SymbolChain<A> symbols = ArraySymbolChain.newChain(this.symbols, input);
		Symbol<A> symbol = symbolIndex.get(input);
		Preconditions.checkNotNull(symbol);

		Optional<Transition<E, A>> opt = automaton.transition(state, symbol);
		if (opt.isPresent()) {
			Transition<E, A> transition = opt.get();
			helper.preChange(this, transition, symbols);
			
			A changedInput = helper.change(this, transition, symbols);
			if (changedInput != null) {
				//till now, there is no change in machine internal state so the following line is ok.
				return input(changedInput);
			} else {
				this.symbols = symbols;
				this.state = transition.target();
				if (automaton.outputStates().contains(state)) {
					helper.acceptInput(this, symbols, state);
				}
				return helper.postChange(this, transition, symbols);
			}
		} else {
			resetInput(); 
			return false;
		} 
	}
	
	@Override
	public SymbolChain<A> acceptInput() {
		if (!automaton.outputStates().contains(state)) {
			throw new IllegalStateException("Cannot accept a non-output state " + state);
		}
		SymbolChain<A> symbols = this.symbols;
		this.symbols = EmptySymbolChain.getInstance();
		return symbols;
	}

	@Override
	public SymbolChain<A> resetInput() {
		if (!automaton.outputStates().contains(state)) {
			helper.rejectInput(this, symbols, state);
		}
		
		state = automaton.initialState();
		SymbolChain<A> symbols = this.symbols;
		this.symbols = EmptySymbolChain.getInstance();
		return symbols;
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
	public void addStateChangeListener(StateChangeListener<E,A> listener) {
		helper.addStateChangeListener(listener);
	}

	@Override
	public void addStateChangeListener(E state, StateChangeListener<E, A> listener) {
		for (State<E> state2 : stateIndex.get(state)) {
			helper.addStateChangeListener(state2, listener);
		}
	}

	@Override
	public void removeStateChangeListener(StateChangeListener<E, A> listener) {
		helper.removeStateChangeListener(listener);
	}

	@Override
	public void removeStateChangeListener(E state, StateChangeListener<E, A> listener) {
		for (State<E> state2 : stateIndex.get(state)) {
			helper.removeStateChangeListener(state2, listener);
		}
	}

	@Override
	public void addTransitionListener(TransitionListener<E, A> listener) {
		helper.addTransitionListener(listener);
	}

	@Override
	public void addTransitionListener(A input, TransitionListener<E, A> listener) {
		helper.addTransitionListener(symbolIndex.get(input), listener);
	}

	@Override
	public void removeTransitionListener(TransitionListener<E, A> listener) {
		helper.removeTransitionListener(listener);
	}

	@Override
	public void removeTransitionListener(A input, TransitionListener<E, A> listener) {
		helper.removeTransitionListener(symbolIndex.get(input), listener);
	}

	@Override
	public void addStateMachineListener(StateMachineListener<E, A> listener) {
		helper.addStateMachineListener(listener);
	}

	@Override
	public void removeStateMachineListener(StateMachineListener<E, A> listener) {
		helper.removeStateMachineListener(listener);
	}
}
