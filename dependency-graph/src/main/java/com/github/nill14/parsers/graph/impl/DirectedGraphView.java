package com.github.nill14.parsers.graph.impl;

import java.util.Set;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

public class DirectedGraphView<V, E extends GraphEdge<V>> implements DirectedGraph<V, E> {

	
	private final DefaultDirectedGraph<V, E> graph;
	private final Set<V> excluded;
	private final ExcludedPredicate<V, E> edgePredicate;

	public DirectedGraphView(DefaultDirectedGraph<V, E> graph, Set<V> excluded) {
		this.graph = graph;
		this.excluded = excluded;
		this.edgePredicate = new ExcludedPredicate<>(excluded);
	}

	@Override
	public Set<V> nodes() {
		return Sets.difference(graph.nodes(), excluded);
	}

	@Override
	public Set<E> edges() {
		return FluentIterable.from(graph.edges()).filter(edgePredicate).toSet();
	}

	@Override
	public Set<E> successorEdges(V vertex) {
		return FluentIterable.from(graph.successorEdges(vertex)).filter(edgePredicate).toSet();
	}

	@Override
	public Set<E> predecessorEdges(V vertex) {
		return FluentIterable.from(graph.predecessorEdges(vertex)).filter(edgePredicate).toSet();
	}

	@Override
	public Set<V> successors(V vertex) {
		return Sets.difference(graph.successors(vertex), excluded);
	}

	@Override
	public Set<V> predecessors(V vertex) {
		return Sets.difference(graph.predecessors(vertex), excluded);
	}

	@Override
	public boolean hasPredecessors(V vertex) {
		return !Sets.difference(graph.predecessors(vertex), excluded).isEmpty();
	}

	@Override
	public boolean hasSucccessors(V vertex) {
		return !Sets.difference(graph.successors(vertex), excluded).isEmpty();
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
	public DirectedGraph<V, E> withoutExcluded(Set<V> excluded) {
		throw new UnsupportedOperationException();
	}
	

	private static class ExcludedPredicate<V, E extends GraphEdge<V>> implements Predicate<E> {
		
		private final Set<V> excluded;

		public ExcludedPredicate(Set<V> excluded) {
			this.excluded = excluded;
		}
		
		@Override
		public boolean apply(E e) {
			return !excluded.contains(e.source()) && !excluded.contains(e.target());
		}
	}
}