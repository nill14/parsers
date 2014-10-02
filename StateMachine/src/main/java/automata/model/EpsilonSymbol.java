package automata.model;

import automata.Symbol;

public class EpsilonSymbol implements Symbol {
  
  private static final EpsilonSymbol INSTANCE = new EpsilonSymbol();
  
  public static Symbol epsilon() {
    return INSTANCE;
  }
  
  private EpsilonSymbol() {
  }
  

  @Override
  public String toString() {
    return name();
  }
  
  public String name() {
    return "ε";
  }
  
  @Override
  public boolean isEpsilon() {
    return true;
  }


}
