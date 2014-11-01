package graph.impl;

import java.util.Set;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.DirectedGraphBuilder;
import com.github.nill14.parsers.graph.GraphEdge;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;

public class DefaultDirectedGraph<V, E extends GraphEdge<V>> implements DirectedGraph<V, E> {


	private final ImmutableSet<V> nodes;
	private final ImmutableSet<E> edges;
	private final ImmutableSetMultimap<V, E> predecessorEdges;
	private final ImmutableSetMultimap<V, E> successorEdges;
	private final ImmutableSetMultimap<V, V> predecessors;
	private final ImmutableSetMultimap<V, V> successors;
	

	public DefaultDirectedGraph(Builder<V, E> builder) {
		nodes = ImmutableSet.copyOf(builder.nodes);
		edges = ImmutableSet.copyOf(builder.edges);
		
		ImmutableSetMultimap.Builder<V, E> predecessorEdges = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<V, E> successorEdges = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<V, V> predecessors = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<V, V> successors = ImmutableSetMultimap.builder();

		for (E edge : edges) {
			if (!nodes.contains(edge.source())) {
				throw new IllegalStateException("Edge has invalid source: "+ edge.source());
			}
			
			if (!nodes.contains(edge.target())) {
				throw new IllegalStateException("Edge has invalid target: "+ edge.target());
			}
			
			predecessorEdges.put(edge.target(), edge);
			successorEdges.put(edge.source(), edge);
			predecessors.put(edge.target(), edge.source());
			successors.put(edge.source(), edge.target());
		}
		
		this.predecessorEdges = predecessorEdges.build();
		this.successorEdges = successorEdges.build();
		
		this.predecessors = predecessors.build();
		this.successors = successors.build();
	}
	
	@Override
	public Set<V> nodes() {
		return nodes;
	}

	@Override
	public Set<V> successors(V node) {
		return successors.get(node);
	}

	@Override
	public Set<V> predecessors(V node) {
		return predecessors.get(node);
	}

	@Override
	public Set<E> predecessorEdges(V node) {
		return predecessorEdges.get(node);
	}
	
	@Override
	public Set<E> successorEdges(V node) {
		return successorEdges.get(node);
	}
	
	@Override
	public Set<E> edges() {
		return edges;
	}
	
	@Override
	public boolean hasPredecessors(V vertex) {
		return predecessors.containsKey(vertex);
	}
	
	@Override
	public boolean hasSucccessors(V vertex) {
		return successors.containsKey(vertex);
	}
	
	@Override
	public <X> Set<X> predecessors(V vertex, Function<V, X> transform) {
		return FluentIterable.from(predecessors(vertex)).transform(transform).toSet();
	}
	
	@Override
	public <X> Set<X> successors(V vertex, Function<V, X> transform) {
		return FluentIterable.from(successors(vertex)).transform(transform).toSet();
	}
	
	@Override
	public DirectedGraph<V, E> withExcluded(Set<V> excluded) {
		return new DirectedGraphView<V, E>(this, excluded);
	}
	
	
	public static final <V, E extends GraphEdge<V>> DirectedGraphBuilder<V, E> builder() {
		return new Builder<>();
	}
	
	public static class Builder<V, E extends GraphEdge<V>> implements DirectedGraphBuilder<V, E> {

		private Set<V> nodes;
		private Set<E> edges;

		@Override
		public DirectedGraphBuilder<V, E> nodes(Set<V> nodes) {
			this.nodes = nodes;
			return this;
		}

		@Override
		public DirectedGraphBuilder<V, E> edges(Set<E> edges) {
			this.edges = edges;
			return this;
		}
		
		@Override
		public DirectedGraph<V, E> build() {
			return new DefaultDirectedGraph<>(this);
		}
	}


	
}
