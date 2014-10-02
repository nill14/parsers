package util;

public interface ImmutableStack<E> extends Iterable<E> {

  static <T> ImmutableStack<T> empty() {
    return FuncStack.empty();
  }
  
  int size();
  
  boolean isEmpty();
  
  E head();
  
  ImmutableStack<E> tail();
  
  ImmutableStack<E> add(E element);
  
}
