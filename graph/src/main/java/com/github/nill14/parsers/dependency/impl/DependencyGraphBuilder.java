package com.github.nill14.parsers.dependency.impl;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.IDependencyGraphFactory;
import com.github.nill14.parsers.dependency.IModule;
import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.impl.DefaultDirectedGraph;
import com.github.nill14.parsers.graph.impl.EvaluatedGraphEdge;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class DependencyGraphBuilder<K, M> implements IDependencyGraphFactory<M> {
	
	private static final Logger log = LoggerFactory.getLogger(DependencyGraphBuilder.class);

	private final DirectedGraph<M, GraphEdge<M>> graph;
	private final ImmutableMap<M, Integer> priorityMap;
	
	
	public static <K, M> IDependencyGraphFactory<M> newInstance(DirectedGraph<M, GraphEdge<M>> graph, Map<M, Integer> priorityMap) {
		return new DependencyGraphBuilder<K, M>(graph, priorityMap);
	}
	
	public static <K, M extends IModule<K>> IDependencyGraphFactory<M> newInstance(Set<M> modules) throws UnsatisfiedDependencyException {
		Function<M, M> identity = Functions.<M>identity();
		return new DependencyGraphBuilder<K, M>(modules, identity);
	}
	
	public static <K, M> IDependencyGraphFactory<M> newInstance(Set<M> modules, Function<M, ? extends IModule<K>> adapterFunction) throws UnsatisfiedDependencyException {
		return new DependencyGraphBuilder<K, M>(modules, adapterFunction);
	}

	private DependencyGraphBuilder(DirectedGraph<M, GraphEdge<M>> graph, Map<M, Integer> priorityMap) {
		this.graph = graph;
		this.priorityMap = ImmutableMap.copyOf(priorityMap);
	}
	
	private DependencyGraphBuilder(Set<M> modules, Function<M, ? extends IModule<K>> adapterFunction) throws UnsatisfiedDependencyException {
		
		ImmutableMap.Builder<M, Integer> priorityMapBuilder = ImmutableMap.builder();
		ImmutableSetMultimap.Builder<K, M> consumersBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<K, M> consumersOptBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<K, M> producersBuilder = ImmutableSetMultimap.builder();
		
		for (M module : modules) {
			IModule<K> node = adapterFunction.apply(module);
			for (K consumer : node.getRequiredDependencies()) {
				consumersBuilder.put(consumer, module);
			}

			for (K consumer : node.getOptionalDependencies()) {
				consumersOptBuilder.put(consumer, module);
			}
			
			for (K producer : node.getOptionalProviders()) {
				producersBuilder.put(producer, module);
			}
			
			priorityMapBuilder.put(module, node.getModulePriority());
		}
		
		SetMultimap<K, M> consumers = consumersBuilder.build();
		SetMultimap<K, M> consumersOpt = consumersOptBuilder.build();
		SetMultimap<K, M> producers = producersBuilder.build();
		Builder<GraphEdge<M>> edges = ImmutableSet.builder();
		
		Set<K> keys = Sets.union(consumers.keySet(), consumersOpt.keySet());
		for (K key : keys) {
			Set<M> from = producers.get(key);
			Set<M> to = consumers.get(key);
			
			for (M target : to) {
				if (from.isEmpty()) {
					throw new UnsatisfiedDependencyException(target, key);
				}
				for (M source : from) {
					GraphEdge<M> edge = EvaluatedGraphEdge.edge(source, target);
					log.info("({}) {} -> {}", key, source, target);
					edges.add(edge);
				}
			}
			
			Set<M> toOpt = consumersOpt.get(key);
			
			for (M target : toOpt) {
				for (M source : from) {
					GraphEdge<M> edge = EvaluatedGraphEdge.edge(source, target);
					log.info("({} opt) {} -> {}", key, source, target);
					edges.add(edge);
				}
			}
		}
		
		priorityMap = priorityMapBuilder.build();
		graph = DefaultDirectedGraph.<M, GraphEdge<M>>builder()
			.nodes(modules)
			.edges(edges.build())
			.build();
	}
	
	@Override
	public DirectedGraph<M, GraphEdge<M>> getDirectedGraph() {
		return graph;
	}
	
	@Override
	public Collection<Deque<M>> getGraphCycles() {
		return new GraphCycleDetector<>(graph).getNontrivialCycles();
	}
	
	@Override
	public IDependencyGraph<M> createDependencyGraph() throws CyclicGraphException {
		return new DependencyGraph<>(graph, newPriorityFunction());
	}

	private Function<M, Integer> newPriorityFunction() {
		return new Function<M, Integer>() {
			@Override
			public Integer apply(M input) {
				Integer val = priorityMap.get(input);
				if (val == null) {
					return 0;
				}
				return val;
			}
		};
	}
	
}
