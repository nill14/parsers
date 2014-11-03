package com.github.nill14.parsers.graph.utils;

import static java.lang.Math.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
/**
 * 
 */
public class LongestPathTopoSorter<V, E extends GraphEdge<V>> {

	private final DirectedGraph<V, E> graph;
	private final ImmutableMap<V, Vertex<V>> nodeIndex;
	private final Function<E, Integer> eval;
	private final List<Vertex<V>> vertices;
	
	
	public LongestPathTopoSorter(DirectedGraph<V, E> acyclicDirectedGraph) {
		this(acyclicDirectedGraph, new Function<E, Integer>() {
			@Override
			public Integer apply(E input) {
				return 1;
			}
		});
	}
	
	public LongestPathTopoSorter(DirectedGraph<V, E> acyclicDirectedGraph, Function<E, Integer> eval) {
		this.graph = acyclicDirectedGraph;
		this.eval = eval;
		vertices = Lists.newArrayList();
		for (V node : graph.nodes()) {
		  vertices.add(new Vertex<V>(node));
		}
		
		nodeIndex = Maps.uniqueIndex(vertices, new Function<Vertex<V>, V>() {
			@Override
			public V apply(Vertex<V> input) {
				return input.node;
			}
		});
	}

	/**
	 * The result is topologically sorted
	 * @return A set of (Vertex, count) pairs
	 * @throws CyclicGraphException when the graph contains cycles
	 */
	public LinkedHashMap<V, Integer> getLongestPathMap() throws CyclicGraphException {
		LinkedList<Vertex<V>> vertices = topologicalOrdering();
		Collections.reverse(vertices);
		for (Vertex<V> v : vertices) {
			visitCount(v);
		}
		
		Collections.sort(vertices, new Comparator<Vertex<V>>() {
            @Override
            public int compare(Vertex<V> o1, Vertex<V> o2) {
              return o2.depth - o1.depth;
            }
		});
		
		LinkedHashMap<V, Integer> result = new LinkedHashMap<>();
		for (Vertex<V> v : vertices) {
			result.put(v.node, v.depth);
		}
		return result;
	}
	
	private void visitCount(Vertex<V> n) {
		Collection<E> next = getNextEdges(n);
		if (next.isEmpty()) {
			n.depth = 0;
		} else {
			int max = 0;
			for (E edge : next) {
				Vertex<V> m = getVertex(edge.target());
				int count = eval.apply(edge) + m.depth;
				max = max(max, count);
			}
			n.depth = max;
		}
	}
	
	/**
	 * 
	 * @return a topologically ordered list
	 * @throws CyclicGraphException when the graph contains cycles
	 */
	public List<V> getLongestPathTopologicalOrdering() throws CyclicGraphException {
		return Lists.newArrayList(getLongestPathMap().keySet());
	}

	/**
	 * 
	 * @return a topologically ordered list
	 * @throws CyclicGraphException when the graph contains cycles
	 */
	public List<V> getTopologicalOrdering() throws CyclicGraphException {
		return FluentIterable.from(topologicalOrdering())
			.transform(new Function<Vertex<V>, V>() {

				@Override
				public V apply(Vertex<V> input) {
					return input.node;
				}
			}).toList();
	}
	
	
//	L ← Empty list that will contain the sorted nodes
//	while there are unmarked nodes do
//	    select an unmarked node n
//	    visit(n) 
//	function visit(node n)
//	    if n has a temporary mark then stop (not a DAG)
//	    if n is not marked (i.e. has not been visited yet) then
//	        mark n temporarily
//	        for each node m with an edge from n to m do
//	            visit(m)
//	        mark n permanently
//	        unmark n temporarily
//	        add n to head of L
	
	private LinkedList<Vertex<V>> topologicalOrdering() throws CyclicGraphException {
		LinkedList<Vertex<V>> list = Lists.newLinkedList();
		Set<Vertex<V>> visited = Sets.newHashSet();
		
		for (Vertex<V> v : vertices) {
			visitUnsorted(v, visited, list);
		}
		
		return list;
	}
	
	private void visitUnsorted(Vertex<V> n, Set<Vertex<V>> visited, Deque<Vertex<V>> list) throws CyclicGraphException {
		if (n.tempMark) {
			throw new CyclicGraphException(graph,
					"is not DAG - directed acyclic graph - contains cycles");
		} else if (visited.contains(n)) {
			return;
		} else {
			n.tempMark = true;
			Collection<Vertex<V>> next = getNext(n);
			for (Vertex<V> m : next) {
				visitUnsorted(m, visited, list);
			}
			visited.add(n);
			n.tempMark = false;
			list.push(n);
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
	
	private Collection<E> getNextEdges(Vertex<V> v) {
		return graph.successorEdges(v.node);
	}
	
	private <T> Vertex<V> getVertex(V node) {
		return nodeIndex.get(node);
	}
	  
	private static class Vertex<V> {
		public int depth;
		public boolean tempMark;
		private final V node;

		public Vertex(V node) {
			this.node = node;
			this.depth = 0;
			this.tempMark = false;
		}
		
		@Override
		public String toString() {
			return String.format("%s %d %b", node, depth, tempMark);
		}
	}
	
}
