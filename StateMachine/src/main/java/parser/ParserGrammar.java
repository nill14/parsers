package parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import util.FileReader;

import com.google.common.collect.ImmutableList;

public class ParserGrammar {
  
  public static void main(String[] args) throws IOException {
    new ParserGrammar().doSth();
  }
  
  private List<Lexeme> lexemes = ImmutableList.of(
      Lexeme.regex("WHITE_SPACE", "\\s+"),
      Lexeme.regex("COMMENT", "#(.*)"),
      Lexeme.regex("FUNCTION2", "(?U)\\([\\p{Alnum}_]+\\s*,\\s*[\\p{Alnum}_]+\\)\\s*->\\s*[\\p{Alnum}_]+"),
      Lexeme.regex("LEXEMES_BEGIN", "lexemes\\s*\\{"),
      Lexeme.regex("IGNORE_BEGIN", "ignore lexemes\\s*\\{"),
      Lexeme.regex("END_TAG", "}"),
      Lexeme.regex("PROPERTY", "([\\p{Alnum}_]+)\\s*=(.*)"),
      Lexeme.regex("IDENTIFIER", "([\\p{Alnum}_]+)")
//      (A, 0) -> B
  );
  
  
  public void doSth() throws IOException {
    InputStream isGrammar = new FileReader().openClasspathResource(getClass(), "parser/nfaEpsilonGrammar.txt");
    InputStream isInput = new FileReader().openClasspathResource(getClass(), "input/nfaEpsilonInput.txt");
    
    LexicalAnalyzer inputProcessor = new Tokenizer(lexemes);
    Scanner scanner = new Scanner(isGrammar);
    inputProcessor.tokenize(scanner).forEach(t -> System.out.println(t));
  }
}
