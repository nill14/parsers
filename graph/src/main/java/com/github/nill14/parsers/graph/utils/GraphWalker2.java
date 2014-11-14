package com.github.nill14.parsers.graph.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.GraphWalker;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;


/**
 * 
 *
 */
public class GraphWalker2<V> implements GraphWalker<V> {

	private final Lock lock = new ReentrantLock();
	private ExecutionException exception;
	
	private final Comparator<Vertex<V>> rankingComparator = new Comparator<Vertex<V>>() {
		@Override
		public int compare(Vertex<V> o1, Vertex<V> o2) {
			return Integer.compare(o2.ranking, o1.ranking);
		}
	};
	private final BlockingQueue<Vertex<V>> workQueue = new PriorityBlockingQueue<>(11, rankingComparator);
	final Vertex<V> exceptionMarker = new Vertex<>();
	
	private final Semaphore semaphore;
	
	private final Map<V, Vertex<V>> vertices;

	private final DirectedGraph<V, ?> graph;

	public <E extends GraphEdge<V>> GraphWalker2(DirectedGraph<V, E> graph, ImmutableList<V> topoList, Map<V, Integer> rankings) {
		this.graph = graph;
		semaphore = new Semaphore(-graph.nodes().size() + 1);
		
		vertices = Maps.newHashMap();
		Function<V, Vertex<V>> f = Functions.forMap(vertices);
		for (V node : topoList.reverse()) {
			int ranking = rankings.get(node);
			int permits = -graph.predecessors(node).size();
			ImmutableSet<Vertex<V>> successors = ImmutableSet.copyOf(graph.successors(node, f));
			Vertex<V> vertex = new Vertex<V>(node, ranking, permits, successors);
			vertices.put(node, vertex);
			
			if (permits == 0) {
				workQueue.add(vertex);
			}
		}
	}
	
	
	@Override
	public V releaseNext() throws ExecutionException {
		try {
			Vertex<V> vertex = workQueue.take();
			if (vertex == exceptionMarker) {
				checkFailure();
			}
			
			return vertex.node;
		} catch (InterruptedException e) {
			throw new ExecutionException(e);
		}
	}
	
	@Override
	public void onComplete(V node) {
		Vertex<V> vertex = vertices.get(node);
		for (Vertex<V> n : vertex.successors) {
			int permits = n.permits.incrementAndGet();
			if (permits == 0) {
				workQueue.add(n);
			}
		}
		
		semaphore.release();
	}

	@Override
	public void onFailure(V vertex, Exception e) {
		try {
			lock.lock();
			if (exception == null) {
				exception = new ExecutionException(e);
			} else {
				exception.addSuppressed(e);
			}
		} finally {
			lock.unlock();
		}
		workQueue.add(exceptionMarker);
		semaphore.release(graph.nodes().size());
	}
	
	@Override
	public boolean isCompleted() {
		return semaphore.availablePermits() > 0;
	}
	
	@Override
	public int size() {
		return graph.nodes().size();
	}
	
	private void checkFailure() throws ExecutionException {
		try {
			lock.lock();
			if (exception != null) {
				throw exception;
			}
		} finally {
	        lock.unlock();
	    }
	}
	
	@Override
	public void awaitCompletion() throws ExecutionException {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			throw new ExecutionException(e);
		}
		checkFailure();
	}
	
	private static final class Vertex<V> {
		final V node;
		final int ranking;
		final AtomicInteger permits;
		final ImmutableSet<Vertex<V>> successors;

		public Vertex(V node, int ranking, int permits, ImmutableSet<Vertex<V>> successors) {
			this.node = node;
			this.ranking = ranking;
			this.successors = successors;
			this.permits = new AtomicInteger(permits);
		}
		
		public Vertex() {
			node = null;
			ranking = Integer.MAX_VALUE;
			permits = new AtomicInteger();
			successors = ImmutableSet.of();
		}
		
	}
	
}
