package automata.model;

import automata.Symbol;

public class AlphabetSymbol implements Symbol {
  
  public static Symbol of(String name) {
    return new AlphabetSymbol(name);
  }
  
  public static Symbol epsilon() {
    return new AlphabetSymbol(null);
  }
  
  private final String name;

  private AlphabetSymbol(String name) {
    if (name == null) {
      throw new NullPointerException("name");
    }
    this.name = name;
  }

  @Override
  public String toString() {
    return name();
  }
  
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
    AlphabetSymbol other = (AlphabetSymbol) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    return true;
  }
  
  



  @Override
  public boolean isEpsilon() {
    return false;
  }}
