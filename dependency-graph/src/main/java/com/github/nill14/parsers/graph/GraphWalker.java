package com.github.nill14.parsers.graph;

import java.util.concurrent.ExecutionException;

public interface GraphWalker<V> {

	/**
	 * Release a new vertex or blocks until one is available.
	 * @return a such vertex that all predecessors are completed
	 * @throws ExecutionException when a vertex was not completed successfully.
	 */
	V releaseNext() throws ExecutionException;

	/**
	 * Marks the vertex as completed.
	 * @param vertex The vertex to be marked complete
	 */
	void onComplete(V vertex);

	/**
	 * Marks the vertex execution as failed. The subsequent executions are skipped.
	 * The exceptions are collected within an {@link ExecutionException}
	 * @param vertex The vertex
	 * @param e The exception
	 */
	void onFailure(V vertex, Exception e);

	/**
	 * 
	 * @return whether all executions had completed.
	 */
	boolean isCompleted();
	
	/**
	 * 
	 * @return The count of graph vertices
	 */
	int size();

	/**
	 * Blocks until all executions are completed.
	 * @throws ExecutionException when any execution has failed.
	 */
	void awaitCompletion() throws ExecutionException;

}