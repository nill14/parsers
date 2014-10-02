package automata.model;

import automata.State;
import automata.Symbol;


public class Transition {

  private final State targetNode;
  private final State sourceNode;
  private final Symbol symbol;

  public Transition(State sourceNode, State targetNode, Symbol symbol) {
    this.sourceNode = sourceNode;
    this.targetNode = targetNode;
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return String.format("(%s, %s) -> %s", sourceNode, symbol, targetNode);
  }
  
  public boolean accept(Symbol symbol) {
    return this.symbol.equals(symbol);
  }

  public State targetState() {
    return targetNode;
  }

  public State sourceState() {
    return sourceNode;
  }
  
  public Symbol symbol() {
    return symbol;
  }
  
  
}
