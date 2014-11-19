package com.github.nill14.parsers.dependency.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import com.github.nill14.parsers.dependency.IConsumer;
import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.GraphWalker;
import com.github.nill14.parsers.graph.utils.GraphWalker3;
import com.github.nill14.parsers.graph.utils.LongestPathTopoSorter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

class DependencyGraph<M> implements IDependencyGraph<M> {
	
	private final Set<M> modules;
	private final DirectedGraph<M, GraphEdge<M>> graph;
	private final LinkedHashMap<M, Integer> moduleRankings;
	
	private final ImmutableList<M> topologicalOrdering;
	private final Map<M, DependencySet<M>> dependencySets = new ConcurrentHashMap<>();
	
	public DependencyGraph(DirectedGraph<M, GraphEdge<M>> graph) throws CyclicGraphException {
		this.graph = graph;
		this.modules = graph.nodes();
		moduleRankings = new LongestPathTopoSorter<>(graph).getLongestPathMap();
		topologicalOrdering = ImmutableList.copyOf(moduleRankings.keySet());
	}
	
	public DependencyGraph(DirectedGraph<M, GraphEdge<M>> graph, Function<M, Integer> priorityFunction) throws CyclicGraphException {
		this.graph = graph;
		this.modules = graph.nodes();
		moduleRankings = new LongestPathTopoSorter<>(graph).getLongestPathMap(priorityFunction);
		topologicalOrdering = ImmutableList.copyOf(moduleRankings.keySet());
		
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
	public Set<M> getDirectDependencies(M module) {
		return graph.predecessors(module);
	}
	
	@Override
	public Set<M> getAllDependencies(M module) {
		DependencySet<M> dependencySet = dependencySets.get(module);
		if (dependencySet == null) {
			/*
			 * I don't like sync over exposed object
			 * but this is the fastest alternative.
			 */			
			synchronized (module) {
				dependencySet = dependencySets.get(module);
				if (dependencySet == null) {
					dependencySet = new DependencySet<M>(this, module);
					dependencySets.put(module, dependencySet);
				}
			}
		}
		return dependencySet;
	}
	
	@Override
	public List<M> getTopologicalOrder() {
		return topologicalOrdering;
	}
	
	@Override
	public Map<M, Integer> getModuleRankings() {
		return Maps.newLinkedHashMap(moduleRankings);
	}
	
	@Override
	public void walkGraph(final ExecutorService executor,
			final IConsumer<M> moduleConsumer)
			throws ExecutionException {
		
		int parallelism = Runtime.getRuntime().availableProcessors();
		if (executor instanceof ThreadPoolExecutor) {
			int coreSize = ((ThreadPoolExecutor) executor).getCorePoolSize();
			int maxSize = ((ThreadPoolExecutor) executor).getMaximumPoolSize();
			
			parallelism = Math.max(parallelism, coreSize);
			parallelism = Math.min(parallelism, maxSize);
		}
		walkGraph(executor, moduleConsumer, parallelism);
	}
	
	@Override
	public void iterateTopoOrder(IConsumer<M> moduleConsumer) throws ExecutionException {
		
		for (M module : topologicalOrdering) {
			try {
				moduleConsumer.process(module);
			} catch (Exception e) {
				throw new ExecutionException(e);
			}
		}
	}

	
	public void walkGraph(final ExecutorService executor,
			final IConsumer<M> moduleConsumer, int parallelism)
			throws ExecutionException {
		
		final GraphWalker<M> graphWalker = new GraphWalker3<>(graph, topologicalOrdering, parallelism);
		
		for (int i = 0; i < graphWalker.size(); i++) {
			final M module = graphWalker.releaseNext();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						moduleConsumer.process(module);
						graphWalker.onComplete(module);
					} catch (Exception e) {
						graphWalker.onFailure(module, e);
					}
				}
			});
		}
		graphWalker.awaitCompletion();
	}
}
