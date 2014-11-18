package com.github.nill14.parsers.dependency.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class DependencySet<M> implements Set<M> {

	private final DependencyGraph<M> graph;
	private final M module;
	private volatile Set<M> set;

	public DependencySet(DependencyGraph<M> graph, M module) {
		this.graph = graph;
		this.module = module;
	}
	
	private Set<M> buildSet() {
		Set<M> set = this.set;
		if (set == null) {
			synchronized (this) {
				set = this.set;
				if (set == null) {
					ImmutableSet.Builder<M> builder = ImmutableSet.builder();
					Set<M> directDependencies = graph.getDirectDependencies(module);
					builder.addAll(directDependencies);
					for (M n : directDependencies) {
						builder.addAll(graph.getAllDependencies(n));
					}
					this.set = set = builder.build();
				}
			}
		}
		
		return set;
	}
	
	
	@Override
	public int size() {
		return buildSet().size();
	}

	@Override
	public boolean isEmpty() {
		return graph.getDirectDependencies(module).isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return buildSet().contains(o);
	}

	@Override
	public Iterator<M> iterator() {
		return buildSet().iterator();
	}

	@Override
	public Object[] toArray() {
		return buildSet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return buildSet().toArray(a);
	}

	@Override
	public boolean add(M e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return buildSet().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends M> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return String.format("DependencySet [module=%s]", module);
	}
	
	

}
