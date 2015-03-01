package com.github.nill14.parsers.graph.utils;

import static java.lang.Math.*;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
/**
 * 
 * http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm
 *
 * @param <V> Vertex
 */
public class GraphCycleDetector<V> {

	private final DirectedGraph<V, ?> graph;
	private int index = 1;
	private final Collection<Deque<V>> cycles = Lists.newArrayList();
	private final Deque<Vertex<V>> stack = Lists.newLinkedList();
	private final ImmutableMap<V, Vertex<V>> nodeIndex;
	
	
	public GraphCycleDetector(DirectedGraph<V, ?> graph) {
		this.graph = graph;
		List<Vertex<V>> vertices = Lists.newArrayList();
		for (V node : graph.nodes()) {
		  vertices.add(new Vertex<V>(node));
		}
		
		nodeIndex = Maps.uniqueIndex(vertices, new Function<Vertex<V>, V>() {
			@Override
			public V apply(Vertex<V> input) {
				return input.node;
			}
		});
		
		for (Vertex<V> v : vertices) {
			if (v.index == 0) {
				strongConnect(v);
			}
		}
	}

	/***
	 * 
	 * @return Collection of all cycles in the graph. The trivial (self) cycles are NOT included.
	 */
	public Collection<Deque<V>> getNontrivialCycles() {
		return cycles;
	}
	
	private void strongConnect(Vertex<V> v) {
		// Set the depth index for v to the smallest unused index
		v.index = index;
		v.lowLink = index;
		index += 1;

		stack.push(v);

		// Consider successors of v
		Collection<GraphCycleDetector.Vertex<V>> next = getNext(v);
		for (Vertex<V> w : next) {
			if (w.index == 0) {
				// Successor w has not yet been visited; recurse on it
				strongConnect(w);
				v.lowLink = min(v.lowLink, w.lowLink);

			} else if (stack.contains(w)) {
				// Successor w is in stack S and hence in the current SCC
				v.lowLink = min(v.lowLink, w.lowLink);
			}
		}

		// If v is a root node, pop the stack and generate an SCC
		if (v.lowLink == v.index) {
			Deque<V> scc = Lists.newLinkedList();
			Vertex<V> w;
			do {
				w = stack.pop();
				scc.push(w.node);
			} while (w != v);
			
			
			if (scc.size() > 1) {
				cycles.add(scc);
			}
		}
	}
	  
	private Set<Vertex<V>> getNext(Vertex<V> v) {
		return graph.successors(v.node, new Function<V, Vertex<V>>() {
			@Override
			public Vertex<V> apply(V input) {
				return getVertex(input);
			}
		});
	}
		  
	
	private <T> Vertex<V> getVertex(V node) {
		return nodeIndex.get(node);
	}
	  
	private static class Vertex<V> {
		public int index;
		public int lowLink;
		private final V node;

		public Vertex(V node) {
			this.node = node;
			this.index = 0;
			this.lowLink = 0;
		}

		@Override
		public String toString() {
			return String.format("%s %d.%d", node, index, lowLink);
		}
	}
	
}
