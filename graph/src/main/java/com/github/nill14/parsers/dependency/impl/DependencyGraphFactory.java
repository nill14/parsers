package com.github.nill14.parsers.dependency.impl;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.IModuleDependencyDescriptor;
import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.impl.DefaultDirectedGraph;
import com.github.nill14.parsers.graph.impl.EvaluatedGraphEdge;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class DependencyGraphFactory<K, M>  {
	
	private static final Logger log = LoggerFactory.getLogger(DependencyGraphFactory.class);

	
	public static <K, M> IDependencyGraph<M> fromGraph(
			DirectedGraph<M, GraphEdge<M>> graph, Map<M, Integer> priorityMap) throws UnsatisfiedDependencyException, CyclicGraphException {
		
		return new DependencyGraph<>(graph, newPriorityFunction(ImmutableMap.copyOf(priorityMap)));
	}
	

	public static <K, M extends IModuleDependencyDescriptor<K>> IDependencyGraph<M> newInstance(
			Set<M> modules) 
					throws UnsatisfiedDependencyException, CyclicGraphException {
		
		return newInstance(modules, Functions.<M>identity());
	}
	
	
	public static <K, M> IDependencyGraph<M> newInstance(
			Set<M> modules, Function<M, ? extends IModuleDependencyDescriptor<K>> adapterFunction) 
					throws UnsatisfiedDependencyException, CyclicGraphException {
		
		Function<M, Integer> priorityFunction = newPriorityFunction(modules, adapterFunction);
		DirectedGraph<M, GraphEdge<M>> graph = newGraph(modules, adapterFunction);
		
		return new DependencyGraph<>(graph, priorityFunction);
	}
	
	
	private static <K, M> Function<M, Integer> newPriorityFunction(Set<M> modules, Function<M, ? extends IModuleDependencyDescriptor<K>> adapterFunction) throws UnsatisfiedDependencyException {
		
		ImmutableMap.Builder<M, Integer> priorityMapBuilder = ImmutableMap.builder();
		
		for (M module : modules) {
			IModuleDependencyDescriptor<K> node = adapterFunction.apply(module);
			priorityMapBuilder.put(module, node.getModulePriority());
		}
		
		return newPriorityFunction(priorityMapBuilder.build());
	}
	
	public static <K, M> DirectedGraph<M, GraphEdge<M>> newGraph(
			Set<M> modules, Function<M, ? extends IModuleDependencyDescriptor<K>> adapterFunction) 
					throws UnsatisfiedDependencyException {
		
		ImmutableSetMultimap.Builder<K, M> consumersBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<K, M> consumersOptBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<K, M> producersBuilder = ImmutableSetMultimap.builder();
		
		for (M module : modules) {
			IModuleDependencyDescriptor<K> node = adapterFunction.apply(module);
			for (K consumer : node.getRequiredDependencies()) {
				consumersBuilder.put(consumer, module);
			}

			for (K consumer : node.getOptionalDependencies()) {
				consumersOptBuilder.put(consumer, module);
			}
			
			for (K producer : node.getOptionalProviders()) {
				producersBuilder.put(producer, module);
			}
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
					log.trace("({}) {} -> {}", key, source, target);
					edges.add(edge);
				}
			}
			
			Set<M> toOpt = consumersOpt.get(key);
			
			for (M target : toOpt) {
				for (M source : from) {
					GraphEdge<M> edge = EvaluatedGraphEdge.edge(source, target);
					log.trace("({} opt) {} -> {}", key, source, target);
					edges.add(edge);
				}
			}
		}
		
		return DefaultDirectedGraph.<M, GraphEdge<M>>builder()
			.nodes(modules)
			.edges(edges.build())
			.build();
	}

	private static <M> Function<M, Integer> newPriorityFunction(final ImmutableMap<M, Integer> priorityMap) {
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
