package com.github.nill14.parsers.dependency;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;

public interface IDependencyBuilder<Module extends IDependencyCollector> {

	/**
	 * In the graph a dependency relation is modeled by precursors direction
	 * That is, when sorted topologically, the most left element doesn't depend on anything. 
	 * The foremost right element depends on it's precursors
	 * @return directed (possibly acyclic) graph
	 */
	DirectedGraph<Module, GraphEdge<Module>> getGraph();

	Set<Module> getCollectors();
	
	Collection<Deque<Module>> getCycles();
	
	IDependencyManager<Module> buildWalker() throws CyclicGraphException;
}