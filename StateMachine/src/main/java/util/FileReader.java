package util;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import parser.Lexeme;

public class FileReader {

  public FileReader() {
  }

  public InputStream openClasspathResource(Class<?> clazz, String resource) throws IOException {
    try {
      URL url = clazz.getClassLoader().getResource(resource);
      File file = new File(url.toURI());
      System.out.println(file.getAbsolutePath());
      BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
      return is;
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    catch (FileNotFoundException e) {
      throw e;
    }
  }
  
  public Properties readProperties(Class<?> clazz, String resource) throws IOException {
    InputStream is = openClasspathResource(clazz, resource);
    InputStreamReader reader = new InputStreamReader(is);
    
    Properties properties = new Properties();
    properties.load(reader);
    return properties;
  }
  
  public List<Lexeme> readTokensFromProperties(Class<?> clazz, String resource) throws IOException {
    
    Properties properties = readProperties(clazz, resource);
    
    Stream<Lexeme> stream = properties.entrySet().stream().map( entry -> {
      String id = entry.getKey().toString();
      String regex = entry.getValue().toString();
      if (Strings.isNullOrEmpty(id)) {
        return Lexeme.exact(regex.toUpperCase(), regex);
      }
      else {
        return Lexeme.regex(id, regex);
      }
    });

    return ImmutableList.copyOf(stream.iterator());
  }
  
}
