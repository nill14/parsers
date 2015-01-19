package com.github.nill14.parsers.automaton.impl;

import java.util.Set;
import java.util.SortedSet;

import com.github.nill14.parsers.automaton.model.State;
import com.github.nill14.parsers.automaton.model.Symbol;
import com.github.nill14.parsers.automaton.model.Transition;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.impl.DefaultDirectedGraph;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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
abstract class AbstractAutomaton<E extends Comparable<? super E>, A extends Comparable<? super A>> {

	private final ImmutableSet<State<E>> states;
	private final ImmutableSet<State<E>> outputStates;
	private final TransitionFunction<E, A> transitionFunction;
	private final SortedSet<Symbol<A>> alphabet;
	private final DirectedGraph<State<E>, Transition<E, A>> graph;

	public AbstractAutomaton(AutomatonBuilder<E, A> builder) {
		transitionFunction = builder.transitions.build();
		Set<Transition<E, A>> transitions = transitionFunction.transitions();
		graph = DefaultDirectedGraph.<State<E>, Transition<E, A>>builder()
				.nodes(Sets.newHashSet(builder.nodes))
				.edges(transitions)
				.build();
		
		states = ImmutableSet.copyOf(graph.nodes());
		outputStates = ImmutableSet.copyOf(builder.outputNodes);
		alphabet = transitionFunction.symbols();
	}

	public Set<Symbol<A>> symbols(State<E> state) {
		return transitionFunction.symbols(state); 
	}

	public boolean accept(State<E> state, Symbol<A> input) {
		return !transitionFunction.apply(state, input).isEmpty();
	}

	public Set<State<E>> outputStates() {
		return outputStates;
	}

	public Set<State<E>> states() {
		return states;
	}

	public SortedSet<Symbol<A>> alphabet() {
		return alphabet;
	}
	
	public DirectedGraph<State<E>, Transition<E, A>> toGraph() {
		return graph;
	}
	
	protected TransitionFunction<E, A> transitions() {
		return transitionFunction;
	}
	
}
