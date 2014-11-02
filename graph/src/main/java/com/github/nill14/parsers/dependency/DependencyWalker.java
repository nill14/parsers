package com.github.nill14.parsers.dependency;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.utils.GraphWalker;
import com.github.nill14.parsers.graph.utils.LongestPathTopoSorter;
import com.github.nill14.parsers.graph.utils.ParallelExecutionException;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

class DependencyWalker<Module extends IDependencyCollector> implements IDependencyWalker<Module> {
	
	private final Set<Module> modules;
	private final DirectedGraph<Module, GraphEdge<Module>> graph;
	private final LinkedHashMap<Module, Integer> longestPathMap;
	private final List<Module> topologicalOrdering;
	
	public DependencyWalker(DirectedGraph<Module, GraphEdge<Module>> graph) throws CyclicGraphException {
		this.graph = graph;
		this.modules = graph.nodes();
		longestPathMap = new LongestPathTopoSorter<>(graph).getLongestPathMap();
		topologicalOrdering = ImmutableList.copyOf(longestPathMap.keySet());
	}

	
	@Override
	public DirectedGraph<Module, GraphEdge<Module>> getGraph() {
		return graph;
	}
	
	@Override
	public Set<Module> getCollectors() {
		return modules;
	}
	
	@Override
	public List<Module> getTopologicalOrder() {
		return topologicalOrdering;
	}
	
	@Override
	public String getDependencyHierarchy() {
		return Joiner.on("\n").join(longestPathMap.entrySet());
	}
	
	@Override
	public void walkGraph(final ExecutorService executor,
			final ModuleConsumer<Module> moduleConsumer)
			throws ParallelExecutionException {
		
		final GraphWalker<Module> graphWalker = new GraphWalker<>(graph, topologicalOrdering);
		
		for (final Module module : graphWalker) {
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
	}
	
	@Override
	public void iterateTopoOrder(ModuleConsumer<Module> moduleConsumer) throws ParallelExecutionException {
		GraphWalker<Module> graphWalker = new GraphWalker<>(graph, topologicalOrdering);
		
		for (Module module : graphWalker) {
			try {
				moduleConsumer.process(module);
				graphWalker.onComplete(module);
			} catch (Exception e) {
				throw new ParallelExecutionException(e);
			}
		}
		
	}
	
}
