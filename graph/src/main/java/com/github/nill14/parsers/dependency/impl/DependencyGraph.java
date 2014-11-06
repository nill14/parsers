package com.github.nill14.parsers.dependency.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.IModule;
import com.github.nill14.parsers.dependency.ModuleConsumer;
import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.utils.GraphWalker;
import com.github.nill14.parsers.graph.utils.LongestPathTopoSorter;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

class DependencyGraph<M extends IModule<?>> implements IDependencyGraph<M> {
	
	private final Set<M> modules;
	private final DirectedGraph<M, GraphEdge<M>> graph;
	private final LinkedHashMap<M, Integer> longestPathMap;
	private final List<M> topologicalOrdering;
	
	public DependencyGraph(DirectedGraph<M, GraphEdge<M>> graph) throws CyclicGraphException {
		this.graph = graph;
		this.modules = graph.nodes();
		longestPathMap = new LongestPathTopoSorter<>(graph).getLongestPathMap();
		topologicalOrdering = ImmutableList.copyOf(longestPathMap.keySet());
	}
	
	public DependencyGraph(DirectedGraph<M, GraphEdge<M>> graph, Function<M, Integer> priorityFunction) throws CyclicGraphException {
		this.graph = graph;
		this.modules = graph.nodes();
		longestPathMap = new LongestPathTopoSorter<>(graph).getLongestPathMap(priorityFunction);
		topologicalOrdering = ImmutableList.copyOf(longestPathMap.keySet());
	}

	
	@Override
	public DirectedGraph<M, GraphEdge<M>> getGraph() {
		return graph;
	}
	
	@Override
	public Set<M> getModules() {
		return modules;
	}
	
	@Override
	public List<M> getTopologicalOrder() {
		return topologicalOrdering;
	}
	
	@Override
	public String getPrettyPrint() {
		return Joiner.on("\n").join(longestPathMap.entrySet());
	}
	
	@Override
	public void walkGraph(final ExecutorService executor,
			final ModuleConsumer<M> moduleConsumer)
			throws ExecutionException {
		
		final GraphWalker<M> graphWalker = new GraphWalker<>(graph, topologicalOrdering);
		
		try {
			for (final M module : graphWalker) {
				graphWalker.checkFailure();
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							moduleConsumer.process(module);
							graphWalker.onComplete(module);
						} catch (Exception e) {
							graphWalker.onFailure(e);
						}
					}
				});
			}
			graphWalker.awaitCompletion();
			
		} catch (NoSuchElementException e) {
			//see test GraphWalkerTest#testExhaustException
			graphWalker.checkFailure();
		}
	}
	
	@Override
	public void iterateTopoOrder(ModuleConsumer<M> moduleConsumer) throws ExecutionException {
		GraphWalker<M> graphWalker = new GraphWalker<>(graph, topologicalOrdering);
		
		for (M module : graphWalker) {
			try {
				moduleConsumer.process(module);
				graphWalker.onComplete(module);
			} catch (Exception e) {
				throw new ExecutionException(e);
			}
		}
		
	}
	
}
