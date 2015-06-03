package com.github.nill14.parsers.graph.impl;

import com.github.nill14.parsers.graph.GraphEdge;


public class EvaluatedGraphEdge<V, K> implements GraphEdge<V> {


  public static <V, K> EvaluatedGraphEdge<V, K> edge(V from, V to, K value) {
	  return new EvaluatedGraphEdge<V, K>(from, to, value);
  }
  
  public static <V, K> EvaluatedGraphEdge<V, K> edge(V from, V to) {
	  return new EvaluatedGraphEdge<V, K>(from, to, null);
  }
  
  private final V source;
  private final V target;
  private final K value;
  
  public EvaluatedGraphEdge(V source, V target, /*@Nullable*/ K value) {
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
	
	public K value() {
		return value;
	}
  
}

