package util;

import java.util.Iterator;


public class FuncStack<E> implements ImmutableStack<E>{

  private final E head;
  private final ImmutableStack<E> tail;
  private final int size;

  public FuncStack(E head, ImmutableStack<E> tail) {
    this.head = head;
    this.tail = tail;
    size = tail.size() + 1;
  }

  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public E head() {
    return head;
  }

  @Override
  public ImmutableStack<E> tail() {
    return tail;
  }

  @Override
  public ImmutableStack<E> add(E element) {
    return new FuncStack<>(element, this);
  }
  
  @Override
  public Iterator<E> iterator() {
    return new StackIterator<>(this);
  }
  


  private static class EmptyStack<E> implements ImmutableStack<E> {
    
    public EmptyStack() {
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public E head() {
      throw new IllegalStateException();
    }

    @Override
    public ImmutableStack<E> tail() {
      throw new IllegalStateException();
    }

    @Override
    public ImmutableStack<E> add(E element) {
      return new FuncStack<>(element, this);
    }
    
    @Override
    public Iterator<E> iterator() {
      return new StackIterator<>(this);
    }
    
  }

  @SuppressWarnings("rawtypes")
  private static final ImmutableStack EMPTY = new EmptyStack();
  
  @SuppressWarnings("unchecked")
  public static final <E> ImmutableStack<E> empty() {
    return EMPTY;
  }


  private static class StackIterator<E> implements Iterator<E> {

    private ImmutableStack<E> stack;

    public StackIterator(ImmutableStack<E> stack) {
      this.stack = stack;
    }
    
    @Override
    public boolean hasNext() {
      return !stack.isEmpty();
    }

    @Override
    public E next() {
      E head = stack.head();
      stack = stack.tail();
      return head;
    }
    
  }

}
