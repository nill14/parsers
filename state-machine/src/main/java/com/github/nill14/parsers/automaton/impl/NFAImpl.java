package com.github.nill14.parsers.automaton.impl;

import java.util.Set;

import com.github.nill14.parsers.automaton.NFA;
import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.github.nill14.parsers.automaton.model.Transition;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * <pre>
 * {@code
 * An NFA is represented formally by a 5-tuple, (Q, Σ, Δ, q0, F), consisting of
 * 
 *     a finite set of states Q
 *     a finite set of input symbols Σ
 *     a transition function Δ : Q × Σ → P(Q).
 *     an initial (or start) state q0 ∈ Q
 *     a set of states F distinguished as accepting (or final) states F ⊆ Q.
 * 
 * Here, P(Q) denotes the power set of Q. Let w = a1a2 ... an be a word over the alphabet Σ. The automaton M accepts the word w if a sequence of states, r0,r1, ..., rn, exists in Q with the following conditions:
 * 
 *     r0 = q0
 *     ri+1 ∈ Δ(ri, ai+1), for i = 0, ..., n−1
 *     rn ∈ F.
 * 
 * In words, the first condition says that the machine starts in the start state q0. The second condition says that given each character of string w, the machine will transition from state to state according to the transition function Δ. The last condition says that the machine accepts w if the last input of w causes the machine to halt in one of the accepting states. Otherwise, it is said that the automaton rejects the string. The set of strings M accepts is the language recognized by M and this language is denoted by L(M).
 * 
 * We can also define L(M) in terms of Δ*: Q × Σ* → P(Q) such that:
 * 
 *     Δ*(r, ε)= {r} where ε is the empty string, and
 *     If x ∈ Σ*, a ∈ Σ, and Δ*(r, x)={r1, r2,..., rk} then Δ*(r, xa)= Δ(r1, a)∪...∪Δ(rk, a).
 * 
 * Now L(M) = {w | Δ*(q0, w) ∩ F ≠ ∅}.
 * 
 * Note that there is a single initial state, which is not necessary. Sometimes, NFAs are defined with a set of initial states. There is an easy construction that translates a NFA with multiple initial states to a NFA with single initial state, which provides a convenient notation.
 * 
 * For more elementary introduction of the formal definition see automata theory.
 * 
 * Source: wikipedia
 * }
 * </pre>
 */
public class NFAImpl<E extends Comparable<? super E>, A extends Comparable<? super A>> extends AbstractAutomaton<E, A> implements NFA<E, A> {

	private final ImmutableSet<State<E>> initialStates;

	public NFAImpl(AutomatonBuilder<E, A> builder) {
		super(builder);
		
		initialStates = ImmutableSet.copyOf(builder.inputNodes);
		Preconditions.checkArgument(!initialStates.isEmpty());
	}

	@Override
	public Set<Transition<E, A>> transitions(State<E> state, Symbol<A> symbol) {
		return transitions().apply(state, symbol);
	}
	
	@Override
	public Set<State<E>> initialStates() {
		return initialStates;
	}
	
	@Override
	public String toString() {
		return String
				.format("NFA [initialStates=%s, states=%s, outputStates=%s, alphabet=%s, transitions=%s]",
						initialStates, states(), outputStates(), alphabet(),
						transitions());
	}
	
}
