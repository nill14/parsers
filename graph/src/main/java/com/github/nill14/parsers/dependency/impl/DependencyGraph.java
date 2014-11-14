package com.github.nill14.parsers.dependency.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.github.nill14.parsers.dependency.IConsumer;
import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.GraphWalker;
import com.github.nill14.parsers.graph.utils.GraphWalker1;
import com.github.nill14.parsers.graph.utils.LongestPathTopoSorter;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

class DependencyGraph<M> implements IDependencyGraph<M> {
	
	private final Set<M> modules;
	private final DirectedGraph<M, GraphEdge<M>> graph;
	private final LinkedHashMap<M, Integer> moduleRatings;
	
	private final ImmutableList<M> topologicalOrdering;
	private final ImmutableSetMultimap<M,M> dependencies;
	
	public DependencyGraph(DirectedGraph<M, GraphEdge<M>> graph) throws CyclicGraphException {
		this.graph = graph;
		this.modules = graph.nodes();
		moduleRatings = new LongestPathTopoSorter<>(graph).getLongestPathMap();
		topologicalOrdering = ImmutableList.copyOf(moduleRatings.keySet());
		
		SetMultimap<M, M> dependencies = HashMultimap.create();
		for (M node : topologicalOrdering) {
			Set<M> directDependencies = graph.predecessors(node);
			for (M dependency : directDependencies) {
				dependencies.putAll(node, dependencies.get(dependency));
			}
		}
		this.dependencies = ImmutableSetMultimap.copyOf(dependencies);
	}
	
	public DependencyGraph(DirectedGraph<M, GraphEdge<M>> graph, Function<M, Integer> priorityFunction) throws CyclicGraphException {
		this.graph = graph;
		this.modules = graph.nodes();
		moduleRatings = new LongestPathTopoSorter<>(graph).getLongestPathMap(priorityFunction);
		topologicalOrdering = ImmutableList.copyOf(moduleRatings.keySet());
		
		SetMultimap<M, M> dependencies = HashMultimap.create();
		for (M node : topologicalOrdering) {
			Set<M> directDependencies = graph.predecessors(node);
			for (M dependency : directDependencies) {
				dependencies.put(node, dependency);
				dependencies.putAll(node, dependencies.get(dependency));
			}
		}
		this.dependencies = ImmutableSetMultimap.copyOf(dependencies);
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
		return dependencies.get(module);
	}
	
	@Override
	public List<M> getTopologicalOrder() {
		return topologicalOrdering;
	}
	
	@Override
	public Map<M, Integer> getModuleRankings() {
		return Maps.newLinkedHashMap(moduleRatings);
	}
	
	@Override
	public void walkGraph(final ExecutorService executor,
			final IConsumer<M> moduleConsumer)
			throws ExecutionException {
		
		final GraphWalker<M> graphWalker = new GraphWalker1<>(graph, topologicalOrdering);
	
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
	
}
