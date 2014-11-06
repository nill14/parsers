package com.github.nill14.parsers.dependency;

public class DependencyBuildException extends Exception {
	
	
	private static final long serialVersionUID = -1559542704169025088L;

	private final IDependencyCollector<?> module;
	private final Object dependency;

	public <K> DependencyBuildException(IDependencyCollector<K> module, K dependency) {
		super(String.format("%s misses required dependency %s", module, dependency));
		this.module = module;
		this.dependency = dependency;
	}
	
	@SuppressWarnings("unchecked")
	public <K> IDependencyCollector<K> getModule() {
		return (IDependencyCollector<K>) module;
	}
	
	@SuppressWarnings("unchecked")
	public <K> K getDependency() {
		return (K) dependency;
	}

}
