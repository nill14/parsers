package com.github.nill14.parsers.graph.utils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
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
	
	private final Lock schedulingLock = new ReentrantLock();
	private final List<V> topoList;
	
	private final Lock exceptionLock = new ReentrantLock();
	private ExecutionException exception;
	
	private AtomicInteger lastIndex = new AtomicInteger(0);
	private final Semaphore parallelism;

	private final Lock statusLock = new ReentrantLock();
	private final Set<V> running = Sets.newHashSet();
	private final Set<V> completed = Sets.newHashSet();
	
	private final Lock waitLock = new ReentrantLock();
	private final Condition waitLockCondition = waitLock.newCondition();

	public <E extends GraphEdge<V>> GraphWalker3(DirectedGraph<V, E> graph, List<V> topoList, int parallelism) {
		this.graph = graph;
		this.topoList = Lists.newArrayList(topoList); // make a copy - only we can mutate the list
		this.parallelism = new Semaphore(parallelism);
	}
	
	@Override
	public void onComplete(V vertex) {
		finished(vertex);
		
		//reset index
		lastIndex.set(0);
		
		signal();
		parallelism.release();
	}

	@Override
	public void onFailure(V vertex, Exception e) {
		try {
			exceptionLock.lock();
			if (exception == null) {
				exception = new ExecutionException(e);
			} else {
				exception.addSuppressed(e);
			}

		} finally {
			exceptionLock.unlock();
		}
		signal();
	    parallelism.release();
	}
	
	@Override
	public V releaseNext() throws ExecutionException {
		try {
			parallelism.acquire();
		} catch (InterruptedException e) {
			throw new ExecutionException(e);
		}
		
		while (true) {
			checkFailure();
			try {
				schedulingLock.lock();

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
			} finally {
				schedulingLock.unlock();
			}
			
			// none was found, start waiting
			await();
			lastIndex.set(0);
		}
	}


	
	@Override
	public boolean isCompleted() {
		try {
			statusLock.lock();
			return completed.size() == size(); 
		} finally {
			statusLock.unlock();
		}
	}
	
	@Override
	public int size() {
		return graph.nodes().size();
	}
	
	private void checkFailure() throws ExecutionException {
	    try {
	    	exceptionLock.lock();
	        
	        if (exception != null) {
	          throw exception;
	        }
	        
	    } finally {
	    	exceptionLock.unlock();
	    }
	}
	
	@Override
	public void awaitCompletion() throws ExecutionException {
		while (!isCompleted()) {
			checkFailure();
			await();
		}
		checkFailure();
	}

	private boolean doStartIfPossible(V vertex) {
		Set<V> set = graph.predecessors(vertex);
		try {
			statusLock.lock();
			boolean isReleaseable = set.isEmpty() || completed.containsAll(set);
			if (isReleaseable) {
				running.add(vertex);
			}
			return isReleaseable;
		} finally {
			statusLock.unlock();
		}
	}

	private void finished(V vertex) {
		try {
			statusLock.lock();
			//remove from running
			if (!running.remove(vertex)) {
				throw new IllegalArgumentException("Not running, cannot complete: "+vertex);
			}
			
			//add to completed
			completed.add(vertex);
		} finally {
			statusLock.unlock();
		}
	}
	
	private void signal() {
		try {
			waitLock.lock();
			waitLockCondition.signalAll();
		} finally {
			waitLock.unlock();
		}
	}
	
	private void await() throws ExecutionException {
		try {
			waitLock.lock();
			waitLockCondition.await();
		} catch (InterruptedException e) {
			throw new ExecutionException(e);
		} finally {
			waitLock.unlock();
		}
	}	
}
