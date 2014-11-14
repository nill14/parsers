package com.github.nill14.parsers.graph.utils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.GraphWalker;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class GraphWalker3<V> implements GraphWalker<V> {

	private final DirectedGraph<V, ?> graph;
	private final List<V> topoList;
	private AtomicInteger lastIndex = new AtomicInteger(0);
	private ExecutionException exception;
	
	private final Lock lock2 = new ReentrantLock();
	private final Set<V> running = Sets.newHashSet();
	private final Set<V> completed = Sets.newHashSet();
	
	private final Lock lock = new ReentrantLock();
	private final Condition lockCondition = lock.newCondition();

	public <E extends GraphEdge<V>> GraphWalker3(DirectedGraph<V, E> graph, List<V> topoList) {
		this.graph = graph;
		this.topoList = Lists.newArrayList(topoList); // make a copy - only we can mutate the list
	}
	
	@Override
	public void onComplete(V vertex) {
		finished(vertex);
		
		//reset index
		lastIndex.set(0);
		
		if (lock.tryLock()) {
			try {
				lockCondition.signal();
			} finally {
				lock.unlock();
			}
		}
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
          lockCondition.signal();
          
      } finally {
          lock.unlock();
      }
	}
	
	@Override
	public V releaseNext() throws ExecutionException {
		try {
			lock.lock();
			
			while (true) {
				checkFailure();
				if (topoList.isEmpty()) {
					throw new NoSuchElementException();
				} 
				
				int i = lastIndex.getAndIncrement();
				while (i < topoList.size()) {
					V vertex = topoList.get(i);
					if (doStartIfPossible(vertex)) {
						topoList.remove(i);
						return vertex;
					}
					i = lastIndex.getAndIncrement();
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
			lock2.lock();
			return completed.size() == size(); 
		} finally {
			lock2.unlock();
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

	private boolean doStartIfPossible(V vertex) {
		Set<V> set = graph.predecessors(vertex);
		try {
			lock2.lock();
			boolean isReleaseable = set.isEmpty() || completed.containsAll(set);
			if (isReleaseable) {
				running.add(vertex);
			}
			return isReleaseable;
		} finally {
			lock2.unlock();
		}
	}

	private void finished(V vertex) {
		try {
			lock2.lock();
			//remove from runnning
			if (!running.remove(vertex)) {
				throw new IllegalArgumentException("Not running, cannot complete: "+vertex);
			}
			
			//add to completed
			completed.add(vertex);
		} finally {
			lock2.unlock();
		}
	}
	
}
