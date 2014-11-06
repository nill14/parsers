package com.github.nill14.parsers.dependency;

public class UnsatisfiedDependencyException extends Exception {
	
	
	private static final long serialVersionUID = -1559542704169025088L;

	private final IModule<?> module;
	private final Object dependency;

	public <K> UnsatisfiedDependencyException(IModule<K> module, K dependency) {
		super(String.format("%s misses required dependency %s", module, dependency));
		this.module = module;
		this.dependency = dependency;
	}
	
	@SuppressWarnings("unchecked")
	public <K> IModule<K> getModule() {
		return (IModule<K>) module;
	}
	
	@SuppressWarnings("unchecked")
	public <K> K getDependency() {
		return (K) dependency;
	}

}
