package com.github.nill14.parsers.graph;

import java.util.Collection;
import java.util.Deque;

import com.github.nill14.parsers.graph.utils.GraphCycleDetector;

public class CyclicGraphException extends Exception {

	private static final long serialVersionUID = -7597034956726693125L;
	
	private final DirectedGraph<?, ?> graph;
	
	public CyclicGraphException(DirectedGraph<?, ?> graph, String message) {
		super(message);
		this.graph = graph;
	}
	
	public CyclicGraphException(DirectedGraph<?, GraphEdge<?>> graph) {
		this.graph = graph;
	}
	
	
	@SuppressWarnings("unchecked")
	public <V, E extends GraphEdge<V>> DirectedGraph<V, E> getGraph() {
		return (DirectedGraph<V, E>) graph;
	}
	
	public <V> Collection<Deque<V>> getCycles() {
		DirectedGraph<V, GraphEdge<V>> graph = getGraph();
		return new GraphCycleDetector<>(graph).getNontrivialCycles();
	}
}
