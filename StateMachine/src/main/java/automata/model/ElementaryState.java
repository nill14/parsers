package automata.model;

import automata.State;

public class ElementaryState implements State {
  
  public static ElementaryState of(String name) {
    return new ElementaryState(name);
  }
  
  private final String name;

  private ElementaryState(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name();
  }
  
  @Override
  public String name() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    ElementaryState other = (ElementaryState) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    return true;
  }
  
  
  

}
