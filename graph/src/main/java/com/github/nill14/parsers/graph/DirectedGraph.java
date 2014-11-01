package com.github.nill14.parsers.graph;

import java.util.Set;

import com.google.common.base.Function;

public interface DirectedGraph<V, E extends GraphEdge<V>> {

  Set<V> nodes();
  Set<E> edges();
  
  Set<E> successorEdges(V vertex);
  Set<E> predecessorEdges(V vertex);
  
  Set<V> successors(V vertex);
  Set<V> predecessors(V vertex);
  
  boolean hasPredecessors(V vertex);
  boolean hasSucccessors(V vertex);
  
  <X> Set<X> successors(V vertex, Function<V, X> transform);
  <X> Set<X> predecessors(V vertex, Function<V, X> transform);
  
  DirectedGraph<V, E> withExcluded(Set<V> excluded);
  
  
//  DirectedGraph<T> inverse();
  
  
}
