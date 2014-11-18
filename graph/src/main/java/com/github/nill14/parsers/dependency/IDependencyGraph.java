package com.github.nill14.parsers.dependency;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;

public interface IDependencyGraph<M> {

	/**
	 * In the graph a dependency relation is modeled by precursors direction
	 * That is, when sorted topologically, the most left element doesn't depend on anything. 
	 * The foremost right element depends on it's precursors
	 * @return directed acyclic graph
	 */
	DirectedGraph<M, GraphEdge<M>> getGraph();

	/**
	 * 
	 * @return a set of modules
	 */
	Set<M> getModules();
	
	/**
	 * 
	 * @param module The module having dependencies
	 * @return A set of direct dependencies of M, not considering transitive.
	 */
	Set<M> getDirectDependencies(M module);
	
	/**
	 * 
	 * @param module The module having dependencies
	 * @return A set of all dependencies of M, including transitive.
	 */
	Set<M> getAllDependencies(M module);
	
	/**
	 * 
	 * @return a topologically sorted list of modules
	 */
	List<M> getTopologicalOrder();
	
	/**
	 * Returns module ratings based on longest path algorithm and module priority.
	 * The modules sorted by the module rating descendingly are guaranteed 
	 * to be in topological order. 
	 * 
	 * @return module rankings
	 */
	Map<M, Integer> getModuleRankings();
	
	/**
	 * 
	 * @param executor an executor to be used for executing the closure
	 * @param moduleConsumer a processing closure
	 * @throws ExecutionException when the closure throws an exception
	 */
	void walkGraph(ExecutorService executor, IConsumer<M> moduleConsumer) throws ExecutionException;

	/**
	 * Synchronous version of {@link #walkGraph(ExecutorService, IConsumer)}
	 * The order is guaranteed to be the same as {@link #getTopologicalOrder()}
	 * 
	 * @param moduleConsumer a processing closure
	 * @throws ExecutionException when the closure throws an exception
	 */
	void iterateTopoOrder(IConsumer<M> moduleConsumer) throws ExecutionException;
}
