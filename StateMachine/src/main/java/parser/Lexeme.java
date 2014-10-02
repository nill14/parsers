package parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.MatcherParameter;
import automata.Symbol;

public class Lexeme {
  private final Symbol symbol;
  private final Pattern pattern;

  public static Lexeme regex(String id, String regex) {
    return new Lexeme(id, regex);
  }
  
  public static Lexeme exact(String id, String phrase) {
    String regex = Matcher.quoteReplacement(phrase);
    /*
    * \b -   A word boundary
    */
   if (!regex.isEmpty()) {
     String lastChar = regex.substring(regex.length() - 1);
     if (letterPattern.matcher(lastChar).matches()) {
       regex = regex + "\\b";
     }
   }
    
    return new Lexeme(id, regex);
  }
  
  private static Pattern letterPattern = Pattern.compile("\\w");
  
  private Lexeme(String id, String regex) {
    
    /*
     * http://docs.oracle.com/javase/tutorial/essential/regex/bounds.html
     * \G - The end of the previous match
     */
    if (!regex.startsWith("\\G")) {
      regex = "\\G" + regex;
    }
    
//    /*
//     * \b -   A word boundary
//     */
//    if (!regex.endsWith("\\b")) {
//      String lastChar = regex.substring(regex.length() - 1);
//      if (letterPattern.matcher(lastChar).matches()) {
//        regex = regex + "\\b";
//      }
//    }
    
    symbol = Symbol.of(id);
    pattern = Pattern.compile(regex);
  }
  
  
  
  public Symbol symbol() {
    return symbol;
  }



  public Pattern pattern() {
    return pattern;
  }



  public Token token(String word) {
    Matcher matcher = pattern.matcher(word);
    if (matcher.matches()) {
      MatcherParameter parameter = new MatcherParameter(matcher);
      return new Token(symbol, parameter);
    }
    return new Token(Symbol.epsilon(), new MatcherParameter(word));
  }

  @Override
  public String toString() {
    return String.format("Lexeme [%s, regex=%s]", symbol, pattern);
  }
  
  
}