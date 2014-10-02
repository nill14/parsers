package automata;

import automata.model.AlphabetSymbol;
import automata.model.EpsilonSymbol;

public interface Symbol extends Comparable<Symbol> {
  
  String name();
  
  boolean isEpsilon();
  
  public static Symbol epsilon() {
    return EpsilonSymbol.epsilon();
  }

  public static Symbol of(String name) {
    return AlphabetSymbol.of(name);
  }
  
  default public int compareTo(Symbol o) {
    return this.name().compareTo(o.name());
  }
}
