package automata.model;

import java.util.Set;
import java.util.stream.Collectors;

import automata.State;

import com.google.common.collect.ImmutableSet;

public class CompoundState implements State {
  
  public static CompoundState from(Set<State> states) {
    return new CompoundState(states);
  }
  
  private final ImmutableSet<State> states;
  private final String name;

  private CompoundState(Set<State> states) {
    this.name = states.stream()
        .map(s -> s.name())
        .sorted()
        .collect(Collectors.joining("", "{", "}"));

    this.states = ImmutableSet.copyOf(states);
  }

  @Override
  public String toString() {
    return name();
  }
  
  @Override
  public String name() {
    return name;
  }

  public Set<State> states() {
    return states;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((states == null) ? 0 : states.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CompoundState other = (CompoundState) obj;
    if (states == null) {
      if (other.states != null)
        return false;
    }
    else if (!states.equals(other.states))
      return false;
    return true;
  }


  
  
  
  

}
