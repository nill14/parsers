package automata.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import automata.DFA;
import automata.NFA;
import automata.State;
import automata.Symbol;
import automata.model.Transition;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSortedSet;
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
public class Automaton implements NFA, DFA {

  
  private final State initialState;
  private final ImmutableSet<State> states;
  private final ImmutableSet<State> outputStates;
  private final TransitionFunction transitionFunction;
  private final ImmutableSortedSet<Symbol> alphabet;
  private final ImmutableSetMultimap<State, Symbol> symbols;
  
  public Automaton(NfaBuilder nfaBuilder) {
    initialState = nfaBuilder.inputNode;
    states = ImmutableSet.copyOf(nfaBuilder.nodes);
    outputStates = ImmutableSet.copyOf(nfaBuilder.outputNodes);
    transitionFunction = nfaBuilder.transitions.build();
    
    List<Transition> transitions = transitionFunction.transitions();
    Set<Symbol> symbols = transitions.stream()
                      .map(t -> t.symbol())
                      .collect(Collectors.toSet());
    alphabet = ImmutableSortedSet.copyOf(symbols);
    
    ImmutableSetMultimap.Builder<State, Symbol> stateSymbols = ImmutableSetMultimap.builder();
    transitions.forEach(t -> stateSymbols.put(t.sourceState(), t.symbol()));
    this.symbols = stateSymbols.build();
    
    Preconditions.checkNotNull(initialState);
    if (!states.contains(initialState)){
      throw new IllegalArgumentException("inputNode");
    }
    if (!states.containsAll(outputStates)){
      throw new IllegalArgumentException("outputNodes");
    }
    //TODO check transition nodes
  }

  @Override
  public DFA toDFA() {
    NfaBuilder builder = builder();
    
    return new PowersetHelper(builder, this).create();
  }

  @Override
  public Set<State> transitions(State state, Symbol symbol) {
    return transitionFunction.apply(state, symbol);
  }
  
  @Override
  public Optional<State> transition(State state, Symbol symbol) {
    Set<State> set = transitions(state, symbol);
    if (set.size() == 1) {
      return Optional.of(set.iterator().next());
    }
    else if (set.isEmpty()){
      return Optional.empty();
    } else {
      throw new RuntimeException("Not DFA");
    }
  }
  
  public Stream<Symbol> symbols(State state) {
    return symbols.asMap().getOrDefault(state, ImmutableSet.<Symbol>of()).stream();
  }  
  
  @Override
  public boolean accept(State state, Symbol input) {
    return !transitionFunction.apply(state, input).isEmpty();
  }

  public boolean isOutputState(State state) {
    return outputStates.contains(state);
  }
  
  public boolean isInputState(State state) {
    return initialState.equals(state);
  }
  
  @Override
  public State initialState() {
    return initialState;
  }
  
  @Override
  public Set<State> outputStates() {
    return outputStates;
  }
  
  @Override
  public Set<State> states() {
    return states;
  }
  
  @Override
  public SortedSet<Symbol> alphabet() {
    return alphabet;
  }
  
  @Override
  public String toString() {
    return String.format(
        "NFA [initialState=%s, states=%s, outputStates=%s, alphabet=%s, transitions=%s]",
        initialState,
        states,
        outputStates,
        alphabet,
        transitionFunction);
  }

  public static NfaBuilder builder() {
    return new NfaBuilder();
  }
  
  public static class NfaBuilder {
    private State inputNode;
    private Set<State> nodes = Sets.newHashSet();
    private Set<State> outputNodes = Sets.newHashSet();
    private TransitionFunction.Builder transitions = TransitionFunction.builder();
    private ImmutableListMultimap<State, Transition> sourceTransitions;
    
    private NfaBuilder() {
    }
    
    public NfaBuilder addState(State node) {
      nodes.add(node);
      return this;
    }

    public NfaBuilder addTransition(State source, State target, Symbol input) {
      nodes.add(source);
      nodes.add(target);
      
      transitions.add(new Transition(source, target, input));
      return this;
    }

    public NfaBuilder setInputState(State inputNode) {
      if (this.inputNode != null) {
        throw new RuntimeException("Only one inputState is allowed");//TODO exception
      }
      
      this.inputNode = inputNode;
      nodes.add(inputNode);
      return this;
    }

    public NfaBuilder addOutputState(State outputNode) {
      outputNodes.add(outputNode);
      nodes.add(outputNode);
      return this;
    }

    public Automaton build() {
      return new Automaton(this);
    }
  }

}
