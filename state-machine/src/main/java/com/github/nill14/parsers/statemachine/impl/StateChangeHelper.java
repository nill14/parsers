package com.github.nill14.parsers.statemachine.impl;

import java.util.List;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.github.nill14.parsers.automaton.model.Transition;
import com.github.nill14.parsers.statemachine.StateChangeListener;
import com.github.nill14.parsers.statemachine.StateMachine;
import com.github.nill14.parsers.statemachine.StateMachineListener;
import com.github.nill14.parsers.statemachine.SymbolChain;
import com.github.nill14.parsers.statemachine.TransitionListener;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

public class StateChangeHelper<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	private final List<StateChangeListener<E, A>> stateListeners = Lists.newCopyOnWriteArrayList();
	private final ListMultimap<State<E>, StateChangeListener<E, A>> state2Listeners = ArrayListMultimap.create();
	
	private final List<TransitionListener<E, A>> transitionListeners = Lists.newCopyOnWriteArrayList();
	private final ListMultimap<Symbol<A>, TransitionListener<E, A>> transition2Listeners = ArrayListMultimap.create();
	
	private final List<StateMachineListener<E, A>> machineListeners = Lists.newCopyOnWriteArrayList();
	
	public void preChange(StateMachine<E, A> stateMachine, Transition<E, A> transition, SymbolChain<A> symbols) {
		State<E> exitState = transition.source();
		
		for (StateChangeListener<E, A> listener : stateListeners) {
			listener.exitState(stateMachine, exitState);
		}
		for (StateChangeListener<E, A> listener : state2Listeners.get(exitState)) {
			listener.exitState(stateMachine, exitState);
		}
	}
	
	public A change(StateMachine<E, A> stateMachine, Transition<E, A> transition, SymbolChain<A> symbols) {
		Symbol<A> input = transition.symbol();
		
		for (TransitionListener<E, A> listener : transitionListeners) {
			A symbol = listener.changeState(stateMachine, transition);
			if ( symbol != null && !symbol.equals(transition.symbol()) ) {
				//consider machine rejects input or modifies input
				return symbol;
			}
		}
		for (TransitionListener<E, A> listener : transition2Listeners.get(input)) {
			A symbol = listener.changeState(stateMachine, transition);
			if ( symbol != null && !symbol.equals(transition.symbol()) ) {
				return symbol;
			}
		}	
		
		return null;
	}

	public void acceptInput(StateMachine<E, A> stateMachine,
			SymbolChain<A> symbols, State<E> enterState) {
		if (stateMachine.isOutputState()) {
			for (StateMachineListener<E, A> listener : machineListeners) {
				listener.acceptInput(stateMachine, symbols, enterState);
			}
		}
	}
	
	public boolean postChange(StateMachine<E, A> stateMachine, Transition<E, A> transition, SymbolChain<A> symbols) {
		State<E> enterState = transition.target();
		
		for (StateChangeListener<E, A> listener : stateListeners) {
			A symbol = listener.enterState(stateMachine, enterState);
			if (symbol != null) {
				return stateMachine.input(symbol);
			}
		}
		for (StateChangeListener<E, A> listener : state2Listeners.get(enterState)) { //FIXME concurrent modification exception
			A symbol = listener.enterState(stateMachine, enterState);
			if (symbol != null) {
				return stateMachine.input(symbol);
			}
		}
		
		return true;
	}
	
	public void rejectInput(StateMachine<E, A> stateMachine, SymbolChain<A> symbols, State<E> deadState) {
		for (StateMachineListener<E, A> listener : machineListeners) {
			listener.rejectInput(stateMachine, symbols, deadState);
		}
	}

	public void addStateChangeListener(StateChangeListener<E, A> listener) {
		stateListeners.add(listener);
	}

	public void addStateChangeListener(State<E> state, StateChangeListener<E, A> listener) {
		state2Listeners.put(state, listener);
	}

	public void removeStateChangeListener(StateChangeListener<E, A> listener) {
		stateListeners.remove(listener);
	}

	public void removeStateChangeListener(State<E> state,
			StateChangeListener<E, A> listener) {
		state2Listeners.remove(state, listener);
	}

	public void addTransitionListener(TransitionListener<E, A> listener) {
		transitionListeners.add(listener);
	}

	public void addTransitionListener(Symbol<A> input, TransitionListener<E, A> listener) {
		transition2Listeners.put(input, listener);
	}

	public void removeTransitionListener(TransitionListener<E, A> listener) {
		transitionListeners.remove(listener);
	}

	public void removeTransitionListener(Symbol<A> input, TransitionListener<E, A> listener) {
		transition2Listeners.remove(input, listener);
	}


	public void addStateMachineListener(StateMachineListener<E, A> listener) {
		machineListeners.add(listener);
	}

	public void removeStateMachineListener(StateMachineListener<E, A> listener) {
		machineListeners.remove(listener);
	}

}
