package com.github.nill14.parsers.dependency;

public class UnsatisfiedDependencyException extends Exception {
	
	
	private static final long serialVersionUID = -1559542704169025088L;

	private final Object module;
	private final Object dependency;

	public <M, K> UnsatisfiedDependencyException(M module, K dependency) {
		super(String.format("%s is missing the required dependency %s", module, dependency));
		this.module = module;
		this.dependency = dependency;
	}
	
	@SuppressWarnings("unchecked")
	public <M> M getModule() {
		return (M) module;
	}
	
	@SuppressWarnings("unchecked")
	public <K> K getDependency() {
		return (K) dependency;
	}

}
