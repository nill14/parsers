package com.github.nill14.parsers.dependency;

import java.util.Collection;
import java.util.Deque;

import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;

public interface IDependencyGraphFactory<M> {

	/**
	 * In the graph a dependency relation is modeled by precursors direction
	 * That is, when sorted topologically, the most left element doesn't depend on anything. 
	 * The foremost right element depends on it's precursors
	 * @return directed (possibly acyclic) graph
	 */
	DirectedGraph<M, GraphEdge<M>> getDirectedGraph();

	Collection<Deque<M>> getGraphCycles();
	
	IDependencyGraph<M> createDependencyGraph() throws CyclicGraphException;
}