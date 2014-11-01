package graph.impl;

import com.github.nill14.parsers.graph.GraphEdge;


public class EvaluatedGraphEdge<V> implements GraphEdge<V> {


  public static <V> GraphEdge<V> edge(V from, V to, int value) {
	  return new EvaluatedGraphEdge<V>(from, to, value);
  }
  
  public static <V> GraphEdge<V> edge(V from, V to) {
	  return new EvaluatedGraphEdge<V>(from, to, 1);
  }
  
  private final V source;
  private final V target;
  private final int value;
  
  public EvaluatedGraphEdge(V source, V target, int value) {
	this.source = source;
	this.target = target;
	this.value = value;
  }

	@Override
	public V source() {
		return source;
	}
	
	@Override
	public V target() {
		return target;
	}
	
	public int value() {
		return value;
	}
  
}

