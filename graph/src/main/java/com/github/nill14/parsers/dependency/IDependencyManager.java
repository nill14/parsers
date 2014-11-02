package com.github.nill14.parsers.dependency;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.utils.ParallelExecutionException;

public interface IDependencyManager<Module extends IDependencyCollector> {

	/**
	 * In the graph a dependency relation is modeled by precursors direction
	 * That is, when sorted topologically, the most left element doesn't depend on anything. 
	 * The foremost right element depends on it's precursors
	 * @return directed acyclic graph
	 */
	DirectedGraph<Module, GraphEdge<Module>> getGraph();

	/**
	 * 
	 * @return a set of dependency collectors (modules)
	 */
	Set<Module> getCollectors();
	
	/**
	 * 
	 * @return a topologically sorted list of collectors
	 */
	List<Module> getTopologicalOrder();
	
	String getDependencyHierarchy();

	void walkGraph(ExecutorService executor, ModuleConsumer<Module> moduleConsumer) throws ParallelExecutionException;

	/**
	 * Synchronous version of {@link #walkGraph(ExecutorService, ModuleConsumer)}
	 */
	void iterateTopoOrder(ModuleConsumer<Module> moduleConsumer) throws ParallelExecutionException;
}
