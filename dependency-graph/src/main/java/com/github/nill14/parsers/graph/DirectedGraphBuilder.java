package com.github.nill14.parsers.graph;

import java.util.Set;

public interface DirectedGraphBuilder<V, E extends GraphEdge<V>> {

	DirectedGraphBuilder<V, E> nodes(Set<V> nodes);
	  
	DirectedGraphBuilder<V, E> edges(Set<E> edges);
	  
	DirectedGraph<V, E> build();
	
}
