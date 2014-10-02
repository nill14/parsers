package parser;

import java.util.Scanner;
import java.util.stream.Stream;

public interface LexicalAnalyzer {

  Stream<Token> tokenize(Scanner scanner);
  
}
