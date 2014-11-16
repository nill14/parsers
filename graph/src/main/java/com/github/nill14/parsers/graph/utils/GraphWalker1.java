package com.github.nill14.parsers.graph.utils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.GraphWalker;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class GraphWalker1<V> implements GraphWalker<V> {

	private final DirectedGraph<V, ?> graph;
	private final List<V> topoList;
	private int lastIndex = 0;
	private ExecutionException exception;
	
	private final Set<V> running = Sets.newHashSet();
	private final Set<V> completed = Sets.newHashSet();
	
	private final Lock lock = new ReentrantLock();
	private final Condition lockCondition = lock.newCondition();
	private final Semaphore parallelism;

	public <E extends GraphEdge<V>> GraphWalker1(DirectedGraph<V, E> graph, List<V> topoList, int parallelism) {
		this.graph = graph;
		this.topoList = Lists.newArrayList(topoList); // make a copy - only we can mutate the list
		this.parallelism = new Semaphore(parallelism);
	}
	
	@Override
	public void onComplete(V vertex) {
		try {
			lock.lock();
			
			//reset index
			lastIndex = 0;
			
			//remove from running
			if (!running.remove(vertex)) {
				throw new IllegalArgumentException("Not running, cannot complete: "+vertex);
			}
			
			//add to completed
			completed.add(vertex);
			lockCondition.signalAll();
			
		} finally {
			lock.unlock();
		}
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
			lockCondition.signalAll();

		} finally {
			lock.unlock();
		}
		parallelism.release();
	}
	
	@Override
	public V releaseNext() throws ExecutionException {
		try {
			parallelism.acquire();
		} catch (InterruptedException e) {
			throw new ExecutionException(e);
		}
		
		try {
			lock.lock();
			
			while (true) {
				checkFailure();
				if (topoList.isEmpty()) {
					throw new NoSuchElementException();
				} 
				
				for (; lastIndex < topoList.size(); lastIndex++) {
					V vertex = topoList.get(lastIndex);
					if (isReleaseable(vertex)) {
						topoList.remove(lastIndex);
						running.add(vertex);
						return vertex;
					}
				}
				
				// none was found, start waiting
				try {
					lockCondition.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public boolean isCompleted() {
		try {
			lock.lock();
			return topoList.isEmpty() && running.isEmpty();
		} finally {
			lock.unlock();
		}
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
			lock.lock();
			
			while (!isCompleted()) {
				checkFailure();
				try {
					lockCondition.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			checkFailure();
			
		} finally {
			lock.unlock();
		}
	}
	
	private boolean isReleaseable(V vertex) {
		Set<V> set = graph.predecessors(vertex);
		return set.isEmpty() || completed.containsAll(set);
	}
	
}
