package scoutcenter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class ScoutCenter {

  public static void main(String[] args) throws IOException {

    new ScoutCenter().processFiles(args);
    
  }

  public void processFiles(String[] inputFiles) throws IOException {
    for (String inputFile : inputFiles) {
      
      try(InputStream is = new FileInputStream(inputFile)) {
        
        Reader r = new InputStreamReader(is);
        
        TokenStream tokens = tokenize(is, r);
        
        process(tokens);
        visitor(tokens);
      } 
      
    }
  }
  
  public CommonTokenStream tokenize(InputStream is, Reader r) throws IOException {
    ANTLRInputStream input = new ANTLRInputStream(r);
    ScoutCenterLexer lexer = new ScoutCenterLexer(input);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    
    return tokens;
  }
  
  public void process(TokenStream tokens) {
    System.out.println();
    
    ScoutCenterParser parser = new ScoutCenterParser(tokens);
    ParserRuleContext tree = parser.content();
    ParseTreeWalker walker = new ParseTreeWalker();
    ScoutCenterExtractor extractor = new ScoutCenterExtractor();
    walker.walk(extractor, tree);
  }

  
  public void visitor(TokenStream tokens) {
    System.out.println();
    
    ScoutCenterParser parser = new ScoutCenterParser(tokens);
    ParserRuleContext tree = parser.content();
    MyScoutCenterVisitor visitor = new MyScoutCenterVisitor();
    visitor.visit(tree);
  }
}
