package automata.impl;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import util.Tuple;
import automata.State;
import automata.Symbol;
import automata.model.Transition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;

public class TransitionFunction implements BiFunction<State, Symbol, Set<State>> {

  private final ImmutableSetMultimap<Tuple<Object>,State> map;
  private ImmutableList<Transition> transitions;

  
  private TransitionFunction(Builder builder) {
    map = builder.mapBuilder.build();
    transitions = builder.transitions.build();
  }
  
  @Override
  public Set<State> apply(State state, Symbol symbol) {
    Tuple<Object> key = tuple(state, symbol);
    return map.get(key);
  }
  
  public List<Transition> transitions() {
    return transitions;
  }
  
  public Set<Symbol> symbols(State state) {
    return null;
  }
  
  public Set<Symbol> symbols() {
    return null;
  }
  
  @Override
  public String toString() {
    return transitions.toString();
  }

  private static Tuple<Object> tuple(State state, Symbol symbol) {
    return Tuple.of(state, symbol);
  }

  public static class Builder {
    private ImmutableSetMultimap.Builder<Tuple<Object>, State> mapBuilder = ImmutableSetMultimap.builder();
    private ImmutableList.Builder<Transition> transitions = ImmutableList.<Transition>builder();
    
    public Builder add(Transition transition) {
      Tuple<Object> key = tuple(transition.sourceState(), transition.symbol());
      mapBuilder.put(key, transition.targetState());
      transitions.add(transition);
      return this;
    }
    
    public TransitionFunction build() {
      return new TransitionFunction(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

}
