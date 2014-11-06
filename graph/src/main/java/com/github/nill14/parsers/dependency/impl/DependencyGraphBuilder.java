package com.github.nill14.parsers.dependency.impl;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.IModule;
import com.github.nill14.parsers.dependency.IDependencyGraphBuilder;
import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.impl.DefaultDirectedGraph;
import com.github.nill14.parsers.graph.impl.EvaluatedGraphEdge;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class DependencyGraphBuilder<K, M extends IModule<K>> implements IDependencyGraphBuilder<M> {
	
	private static final Logger log = LoggerFactory.getLogger(DependencyGraphBuilder.class);

	private final DirectedGraph<M, GraphEdge<M>> graph;
	
	public DependencyGraphBuilder(DirectedGraph<M, GraphEdge<M>> graph) {
		this.graph = graph;
	}

	public DependencyGraphBuilder(Set<M> modules) throws UnsatisfiedDependencyException {

		ImmutableSetMultimap.Builder<K, M> consumersBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<K, M> consumersOptBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<K, M> producersBuilder = ImmutableSetMultimap.builder();
		
		for (M node : modules) {
			for (K consumer : node.getRequiredDependencies()) {
				consumersBuilder.put(consumer, node);
			}

			for (K consumer : node.getOptionalDependencies()) {
				consumersOptBuilder.put(consumer, node);
			}
			
			for (K producer : node.getOptionalProviders()) {
				producersBuilder.put(producer, node);
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
		
		graph = DefaultDirectedGraph.<M, GraphEdge<M>>builder()
			.nodes(modules)
			.edges(edges.build())
			.build();
	}
	
	
	@Override
	public DirectedGraph<M, GraphEdge<M>> getGraph() {
		return graph;
	}
	
	@Override
	public Collection<Deque<M>> getCycles() {
		return new GraphCycleDetector<>(graph).getNontrivialCycles();
	}
	
	@Override
	public IDependencyGraph<M> buildDependencyGraph() throws CyclicGraphException {
		return new DependencyGraph<>(graph);
	}
	
}