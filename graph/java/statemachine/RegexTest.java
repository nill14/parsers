package statemachine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import parser.Lexeme;

@SuppressWarnings("nls")
public class RegexTest {

  
  @Test
  public void exactTest() {
    Lexeme exactToken = Lexeme.exact("EXACT", "word one");
    assertTrue(exactToken.pattern().matcher("word one blabla").find());
    
    assertFalse(exactToken.pattern().matcher("blabla word one blabla").find());
    
    assertFalse(exactToken.pattern().matcher("word onee blabla").find());
  }
  
  
  @Test
  public void whitespaceTest() {
    Lexeme whiteSpaceToken = Lexeme.regex("WHITESPACE", "\\s+");
    assertTrue(whiteSpaceToken.pattern().matcher("  sfa sdf").find());
    assertFalse(whiteSpaceToken.pattern().matcher("blabla sfs sdfe").find());
    assertTrue(whiteSpaceToken.pattern().matcher("ab  sfa sdf").find(2));
  }
  
  @Test
  public void wordTest() {
    Lexeme wordToken = Lexeme.regex("WORD", "\\S+");
    assertTrue(wordToken.pattern().matcher("abVzťah sfa wer w").find());
    assertTrue(wordToken.pattern().matcher("ab Vzťah sfa wer w").find(3));
  }
  
  @Test
  public void alphanumTest() {
    String[] test = {"Jean-Marie Le'Blanc", "Żółć", "Ὀδυσσεύς", "原田雅彦", "üöä254ad"};
    Lexeme wordToken = Lexeme.regex("ALPHANUM", "^(?U)[\\p{Alnum}'\\-_ ]+$");
    for (String str : test) {
      assertTrue(str, wordToken.pattern().matcher(str).find());
    }
  }
  
  @Test
  public void word2Test() {
    Lexeme wordToken = Lexeme.exact("WORD", "ab");
    assertFalse(wordToken.pattern().matcher("abc sfa: wer: w:").find());
    assertFalse(wordToken.pattern().matcher("ab Vsfa wab w").find(3));
  }
  @Test
  public void delimTest() {
    Pattern delimPattern = Pattern.compile("$");
    String input = "abc cde wef";
    Matcher matcher = delimPattern.matcher(input);
    assertTrue(matcher.find());
    assertEquals(matcher.start(), input.length());
  }
   
  @Test
  public void commentTest() {
    Lexeme lexeme = Lexeme.regex("COMMENT", "#.*$");
    String[] test = {"#SYMBOL=regex\\nabc"};
    for (String str : test) {
      assertTrue(str, scan(lexeme, str));
    }
  }
  
  public boolean scan(Lexeme lexeme, String source) {
    Scanner scanner = new Scanner(source);
    String token = scanner.findWithinHorizon(lexeme.pattern(), 0);
    scanner.close();
    return token != null;
  }

  

  

}


