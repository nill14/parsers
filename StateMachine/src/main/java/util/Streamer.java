package util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streamer {

  

  private Streamer() {
  }
  
  public static <T> Stream<T> stream(Iterator<T> iterator) {
    Objects.requireNonNull(iterator);
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
            iterator,
            Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
  }
  
  public static<T> Stream<T> iterate(Supplier<T> next, Supplier<Boolean> hasNext) {
    final Iterator<T> iterator = new Iterator<T>() {

        @Override
        public boolean hasNext() {
            return hasNext.get();
        }

        @Override
        public T next() {
            return next.get();
        }
    };
    return stream(iterator);
}
  
  
}
