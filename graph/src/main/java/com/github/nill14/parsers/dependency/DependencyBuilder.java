package com.github.nill14.parsers.dependency;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class DependencyBuilder<K, M extends IDependencyCollector<K>> implements IDependencyBuilder<M> {
	
	private static final Logger log = LoggerFactory.getLogger(DependencyBuilder.class);

	private final Set<M> modules;
	private final DirectedGraph<M, GraphEdge<M>> graph;
	
	public DependencyBuilder(DirectedGraph<M, GraphEdge<M>> graph) {
		this.graph = graph;
		this.modules = graph.nodes();
	}

	public DependencyBuilder(Set<M> collectors) throws DependencyBuildException {
		modules = collectors;

		ImmutableSetMultimap.Builder<K, M> consumersBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<K, M> consumersOptBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<K, M> producersBuilder = ImmutableSetMultimap.builder();
		
		for (M node : collectors) {
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
					throw new DependencyBuildException(target, key);
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
			.nodes(collectors)
			.edges(edges.build())
			.build();
	}
	
	
	@Override
	public DirectedGraph<M, GraphEdge<M>> getGraph() {
		return graph;
	}
	
	@Override
	public Set<M> getCollectors() {
		return modules;
	}
	
	@Override
	public Collection<Deque<M>> getCycles() {
		return new GraphCycleDetector<>(graph).getNontrivialCycles();
	}
	
	@Override
	public IDependencyManager<M> buildManager() throws CyclicGraphException {
		return new DependencyManager<>(graph);
	}
	
}
