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

public class DependencyBuilder<Module extends IDependencyCollector> implements IDependencyBuilder<Module> {
	
	private static final Logger log = LoggerFactory.getLogger(DependencyBuilder.class);

	private final Set<Module> modules;
	private final DirectedGraph<Module, GraphEdge<Module>> graph;
	
	public DependencyBuilder(DirectedGraph<Module, GraphEdge<Module>> graph) {
		this.graph = graph;
		this.modules = graph.nodes();
	}

	public DependencyBuilder(Set<Module> collectors) throws DependencyBuildException {
		modules = collectors;

		ImmutableSetMultimap.Builder<String, Module> consumersBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<String, Module> consumersOptBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<String, Module> producersBuilder = ImmutableSetMultimap.builder();
		
		for (Module node : collectors) {
			for (String consumer : node.getRequiredDependencies()) {
				consumersBuilder.put(consumer, node);
			}

			for (String consumer : node.getOptionalDependencies()) {
				consumersOptBuilder.put(consumer, node);
			}
			
			for (String producer : node.getOptionalProviders()) {
				producersBuilder.put(producer, node);
			}
		}
		
		SetMultimap<String, Module> consumers = consumersBuilder.build();
		SetMultimap<String, Module> consumersOpt = consumersOptBuilder.build();
		SetMultimap<String, Module> producers = producersBuilder.build();
		Builder<GraphEdge<Module>> edges = ImmutableSet.builder();
		
		Set<String> keys = Sets.union(consumers.keySet(), consumersOpt.keySet());
		for (String key : keys) {
			Set<Module> from = producers.get(key);
			Set<Module> to = consumers.get(key);
			
			for (Module target : to) {
				if (from.isEmpty()) {
					throw new DependencyBuildException(target, key);
				}
				for (Module source : from) {
					GraphEdge<Module> edge = EvaluatedGraphEdge.edge(source, target);
					log.info("({}) {} -> {}", key, source, target);
					edges.add(edge);
				}
			}
			
			Set<Module> toOpt = consumersOpt.get(key);
			
			for (Module target : toOpt) {
				for (Module source : from) {
					GraphEdge<Module> edge = EvaluatedGraphEdge.edge(source, target);
					log.info("({} opt) {} -> {}", key, source, target);
					edges.add(edge);
				}
			}
		}
		
		graph = DefaultDirectedGraph.<Module, GraphEdge<Module>>builder()
			.nodes(collectors)
			.edges(edges.build())
			.build();
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
	public Collection<Deque<Module>> getCycles() {
		return new GraphCycleDetector<>(graph).getNontrivialCycles();
	}
	
	@Override
	public IDependencyManager<Module> buildWalker() throws CyclicGraphException {
		return new DependencyManager<>(graph);
	}
	
}
