package parser;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import util.Streamer;

public class Tokenizer implements LexicalAnalyzer {


  private List<Lexeme> lexemes;

  public Tokenizer(List<Lexeme> lexemes) {
    this.lexemes = lexemes;
  }
  
  
  @Override
  public Stream<Token> tokenize(Scanner scanner) {

    return Streamer.iterate(() -> {
      for (Lexeme lexeme : lexemes) {
        Pattern pattern = lexeme.pattern();
        String word = scanner.findWithinHorizon(pattern, 0);
        if (word != null) {
//          String word = scanner.next(pattern);
          Token token = lexeme.token(word);
          return token;
        }
      }
      String next = scanner.nextLine();
      scanner.close();
      String message = "Unmatched word: '" + next + "'"; 
//      System.out.println(message);
      throw new RuntimeException(message);
    },
    () -> {
      boolean hasNext = scanner.hasNext();
      if (!hasNext) {
        scanner.close();
      }
      return hasNext;
    });
  }
  
}