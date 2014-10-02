package parser;

import automata.Symbol;
import util.MatcherParameter;

public class Token {
  
  private final Symbol symbol;
  private final MatcherParameter parameter;

  public Token(Symbol symbol, MatcherParameter parameter) {
    this.symbol = symbol;
    this.parameter = parameter;
  }

  @Override
  public String toString() {
    return String.format("Token [%s, %s]", symbol, parameter);
  }

  public Symbol symbol() {
    return symbol;
  }

  public MatcherParameter parameter() {
    return parameter;
  }

  
  
  
}