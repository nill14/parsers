package com.github.nill14.parsers.graph;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.github.nill14.parsers.graph.impl.EvaluatedGraphEdge;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class CyclicGraphException extends Exception {

	private static final long serialVersionUID = -7597034956726693125L;
	
	private final DirectedGraph<?, ?> graph;
	
	public CyclicGraphException(DirectedGraph<?, ?> graph, String message) {
		super(message);
		this.graph = graph;
	}
	
	public CyclicGraphException(DirectedGraph<?, GraphEdge<?>> graph) {
		this.graph = graph;
	}
	
	@SuppressWarnings("unchecked")
	public <V, E extends GraphEdge<V>> DirectedGraph<V, E> getGraph() {
		return (DirectedGraph<V, E>) graph;
	}
	
	/**
	 * Calculates all non-trivial cycles in the graph. 
	 * @param <V> The vertices of the graph
	 * @return the collection of cycles
	 */
	public <V> Collection<Deque<V>> getGraphCycles() {
		DirectedGraph<V, GraphEdge<V>> graph = getGraph();
		return new GraphCycleDetector<>(graph).getNontrivialCycles();
	}
	
	/**
	 * Calculates all non-trivial cycles in the graph. 
	 * @param <V> The vertices of the graph
	 * @return the collection of cycles, including edge information if available
	 */
	public <V> Collection<String> getDebugCycles() {
		DirectedGraph<V, GraphEdge<V>> graph = getGraph();
		Collection<Deque<V>> cycles = new GraphCycleDetector<>(graph).getNontrivialCycles();
		List<String> result = Lists.newArrayList();
		
		for (Deque<V> cycle : cycles) {
			result.add(transformCycle(ImmutableList.copyOf(cycle)/*.reverse()*/));
		}
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	private <V> Set<GraphEdge<V>> connectingEdges(final V predecessor, final V successor) {
		DirectedGraph<V, GraphEdge<V>> graph = (DirectedGraph<V, GraphEdge<V>>) this.graph;
		Set<GraphEdge<V>> edges = graph.successorEdges(predecessor);
		return FluentIterable.from(edges).filter(new Predicate<GraphEdge<V>>() {

			@Override
			public boolean apply(GraphEdge<V> edge) {
				return edge.target() == successor;
			}
		}).toSet();
	}

	@SuppressWarnings("rawtypes")
	private <V> List<Object> edgeKeys(Set<GraphEdge<V>> edges) {
		return FluentIterable.from(edges).filter(EvaluatedGraphEdge.class)
				.transform(new Function<EvaluatedGraphEdge, Object>() {

				@Override
				public Object apply(EvaluatedGraphEdge edge) {
					return edge.value();
				}
			}).toList();
	}
	

	private <V> String transformCycle(List<V> cycle) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < cycle.size(); i++) {
			V predecessor = cycle.get(i);
			V successor = cycle.get((i + 1) % cycle.size());
			Set<GraphEdge<V>> edges = connectingEdges(predecessor, successor);
			List<Object> keys = edgeKeys(edges);
			
			b.append(predecessor);
			b.append(" - (");
			b.append(Joiner.on(",").join(keys));
			b.append(") - ");
			if (i == cycle.size() - 1) {
				b.append(successor);
			}
		}
		return b.toString();
	}
}
