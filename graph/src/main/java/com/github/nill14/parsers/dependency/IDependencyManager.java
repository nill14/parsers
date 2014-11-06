package com.github.nill14.parsers.dependency;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;

public interface IDependencyManager<M extends IDependencyCollector> {

	/**
	 * In the graph a dependency relation is modeled by precursors direction
	 * That is, when sorted topologically, the most left element doesn't depend on anything. 
	 * The foremost right element depends on it's precursors
	 * @return directed acyclic graph
	 */
	DirectedGraph<M, GraphEdge<M>> getGraph();

	/**
	 * 
	 * @return a set of dependency collectors (modules)
	 */
	Set<M> getCollectors();
	
	/**
	 * 
	 * @return a topologically sorted list of collectors
	 */
	List<M> getTopologicalOrder();
	
	String getDependencyHierarchy();

	/**
	 * 
	 * @param executor an executor to be used for executing the closure
	 * @param moduleConsumer a processing closure
	 * @throws ExecutionException when the closure throws an exception
	 */
	void walkGraph(ExecutorService executor, ModuleConsumer<M> moduleConsumer) throws ExecutionException;

	/**
	 * Synchronous version of {@link #walkGraph(ExecutorService, ModuleConsumer)}
	 * 
	 * @param moduleConsumer a processing closure
	 * @throws ExecutionException when the closure throws an exception
	 */
	void iterateTopoOrder(ModuleConsumer<M> moduleConsumer) throws ExecutionException;
}
