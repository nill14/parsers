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

	private final List<StateChangeListener> stateListeners = Lists.newCopyOnWriteArrayList();
	private final ListMultimap<State<E>, StateChangeListener> state2Listeners = ArrayListMultimap.create();
	
	private final List<TransitionListener> transitionListeners = Lists.newCopyOnWriteArrayList();
	private final ListMultimap<Symbol, TransitionListener> transition2Listeners = ArrayListMultimap.create();
	
	private final List<StateMachineListener> machineListeners = Lists.newCopyOnWriteArrayList();
	
	
	public void changeState(StateMachine<E, A> stateMachine, Transition<E, A> transition, SymbolChain<A> symbols) {
		State<E> exitState = transition.source();
		State<E> enterState = transition.target();
		Symbol<A> input = transition.symbol();
		
		for (StateChangeListener listener : stateListeners) {
			listener.exitState(stateMachine, exitState);
		}
		for (StateChangeListener listener : state2Listeners.get(exitState)) {
			listener.exitState(stateMachine, exitState);
		}
		
		for (TransitionListener listener : transitionListeners) {
			listener.changeState(stateMachine, transition);
		}
		for (TransitionListener listener : transition2Listeners.get(input)) {
			listener.changeState(stateMachine, transition);
		}		
		
		for (StateChangeListener listener : stateListeners) {
			listener.enterState(stateMachine, enterState);
		}
		for (StateChangeListener listener : state2Listeners.get(enterState)) {
			listener.enterState(stateMachine, enterState);
		}
		
		if (stateMachine.isOutputState()) {
			for (StateMachineListener listener : machineListeners) {
				listener.acceptInput(stateMachine, symbols, enterState);
			}
		}
	}
	
	public void resetState(StateMachine<E, A> stateMachine, SymbolChain<A> symbols, State<E> deadState) {
		for (StateMachineListener listener : machineListeners) {
			listener.rejectInput(stateMachine, symbols, deadState);
		}
	}

	public void addStateChangeListener(StateChangeListener listener) {
		stateListeners.add(listener);
	}

	public void addStateChangeListener(State state, StateChangeListener listener) {
		state2Listeners.put(state, listener);
	}

	public void removeStateChangeListener(StateChangeListener listener) {
		stateListeners.remove(listener);
	}

	public void removeStateChangeListener(State state,
			StateChangeListener listener) {
		state2Listeners.remove(state, listener);
	}

	public void addTransitionListener(TransitionListener listener) {
		transitionListeners.add(listener);
	}

	public void addTransitionListener(Symbol<A> input, TransitionListener listener) {
		transition2Listeners.put(input, listener);
	}

	public void removeTransitionListener(TransitionListener listener) {
		transitionListeners.remove(listener);
	}

	public void removeTransitionListener(Symbol<A> input, TransitionListener listener) {
		transition2Listeners.remove(input, listener);
	}


	public void addStateMachineListener(StateMachineListener listener) {
		machineListeners.add(listener);
	}

	public void removeStateMachineListener(StateMachineListener listener) {
		machineListeners.remove(listener);
	}

}
