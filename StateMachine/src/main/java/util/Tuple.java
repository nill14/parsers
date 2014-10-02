package util;

import java.util.Arrays;

public class Tuple<U> {

  private final U[] values;
  
  private Tuple(U[] values) {
    this.values = Arrays.copyOf(values, values.length);;
  }
  
  @SafeVarargs
  public static <U> Tuple<U> of(U... values) {
    return new Tuple<>(values);
  }
  
  /**
   * 
   * @param index index from 1
   * @return
   */
  public U get(int index) {
    return values[index - 1];
  }
  
  public <T extends U> T get(Class<T> clazz, int index) {
    return clazz.cast(get(index));
  }
  
  public Tuple<U> copyWith(int index, U value) {
    Tuple<U> tuple = new Tuple<>(this.values);
    tuple.values[index - 1] = value;
    return tuple;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(values);
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Tuple other = (Tuple) obj;
    if (!Arrays.equals(values, other.values))
      return false;
    return true;
  }
  
  
}
