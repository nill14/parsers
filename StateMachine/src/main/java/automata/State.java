package automata;

import automata.model.ElementaryState;

public interface State {

  
  public static State of(String name) {
    return ElementaryState.of(name);
  }
  
  String name();

  default public int compareTo(State o) {
    return this.name().compareTo(o.name());
  }
  
}
