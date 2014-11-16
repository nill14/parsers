package com.github.nill14.parsers.graph.utils;

import java.util.Map;
import java.util.Set;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


/**
 * 
 *
 */
public class GraphWalker4<V> implements GraphWalker<V> {

	private final Lock lock = new ReentrantLock();
	private ExecutionException exception;
	
	private final BlockingQueue<RankedElement> workQueue = new PriorityBlockingQueue<>();
	
	private final Semaphore countDown;
	private final Semaphore parallelism;

	private final DirectedGraph<V, ?> graph;
	private final Map<V, Integer> rankings;
	private Map<V, AtomicInteger> permits;

	public <E extends GraphEdge<V>> GraphWalker4(DirectedGraph<V, E> graph, ImmutableList<V> topoList, Map<V, Integer> rankings, int parallelism) {
		this.graph = graph;
		this.rankings = rankings;
		countDown = new Semaphore(-graph.nodes().size() + 1);
		
		
		ImmutableMap.Builder<V, AtomicInteger> permitsBuilder = ImmutableMap.builder();
		for (V node : topoList.reverse()) {
			int blockers = graph.predecessors(node).size();
			
			if (blockers == 0) {
				int ranking = rankings.get(node);
				workQueue.add(new RankedElement(node, ranking));
			} else {
				permitsBuilder.put(node, new AtomicInteger(blockers));
			}
		}
		permits = permitsBuilder.build();
		this.parallelism = new Semaphore(parallelism);
	}
	
	
	@Override
	public V releaseNext() throws ExecutionException {
		checkFailure();
		try {
			parallelism.acquire();
			return workQueue.take().node;
		} catch (InterruptedException e) {
			throw new ExecutionException(e);
		}
	}
	
	@Override
	public void onComplete(V node) {
		Set<V> successors = graph.successors(node);
		for (V n : successors) {
			int i = permits.get(n).decrementAndGet();
			if (i == 0) {
				int ranking = rankings.get(n);
				workQueue.add(new RankedElement(n, ranking));
			}
		}
		
		countDown.release();
		parallelism.release();
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
		countDown.release(size());
	}
	
	@Override
	public boolean isCompleted() {
		return countDown.availablePermits() > 0;
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
			countDown.acquire();
		} catch (InterruptedException e) {
			throw new ExecutionException(e);
		}
		checkFailure();
	}
	
	private final class RankedElement implements Comparable<RankedElement> {
		final V node;
		final int ranking;

		public RankedElement(V node, int ranking) {
			this.node = node;
			this.ranking = ranking;
		}

		@Override
		public int compareTo(RankedElement o) {
			return this.ranking - o.ranking;
		}

		@Override
		public String toString() {
			return String.format("%s (%d)", node, ranking);
		}
		
	}
	
}
