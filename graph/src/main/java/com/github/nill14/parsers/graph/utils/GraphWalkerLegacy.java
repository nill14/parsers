package com.github.nill14.parsers.graph.utils;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * 
 *
 * Thread safety:  {@link #releaseNext()}, {@link #hasNext()} is supposed to be called only from a single scheduling thread.
 */
public class GraphWalkerLegacy<V> implements Iterable<V> {

	private final DirectedGraph<V, ?> graph;
	private final List<V> topoList;
	private int lastIndex = 0;
	private ExecutionException exception;
	
	private final Set<V> running = Sets.newHashSet();
	private final Set<V> completed = Sets.newHashSet();
	
	private final Lock lock = new ReentrantLock();
	private final Condition lockCondition = lock.newCondition();

	public <E extends GraphEdge<V>> GraphWalkerLegacy(DirectedGraph<V, E> graph, List<V> topoList) {
		this.graph = graph;
		this.topoList = Lists.newArrayList(topoList); // make a copy - only we can mutate the list
	}
	
	public void onComplete(V vertex) {
		try {
			lock.lock();
			
			//reset index
			lastIndex = 0;
			
			//remove from runnning
			if (!running.remove(vertex)) {
				throw new IllegalArgumentException("Not running, cannot complete: "+vertex);
			}
			
			//add to completed
			completed.add(vertex);
			lockCondition.signal();
			
		} finally {
			lock.unlock();
		}
	}
	
	public void onFailure(Exception e) {
	     try {
          lock.lock();
          if (exception == null) {
        	  exception = new ExecutionException(e);
          } else {
        	  exception.addSuppressed(e);
          }
          lockCondition.signal();
          
      } finally {
          lock.unlock();
      }
	}
	
	public V releaseNext() {
		try {
			lock.lock();
			
			while (true) {
				if (topoList.isEmpty() || exception != null) {
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
	
	public boolean hasNext() {
		try {
			lock.lock();
			return !topoList.isEmpty();
		} finally {
			lock.unlock();
		}
	}
	
	public boolean isCompleted() {
		try {
			lock.lock();
			return topoList.isEmpty() && running.isEmpty();
		} finally {
			lock.unlock();
		}
	}
	
	public void checkFailure() throws ExecutionException {
	      try {
	        lock.lock();
	        if (exception != null) {
	          throw exception;
	        }
	        
	    } finally {
	        lock.unlock();
	    }
	}
	
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
	
	
	@Override
	public Iterator<V> iterator() {
		return new Iterator<V>() {

			@Override
			public boolean hasNext() {
				return GraphWalkerLegacy.this.hasNext();
			}

			@Override
			public V next() {
				return GraphWalkerLegacy.this.releaseNext();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
}
