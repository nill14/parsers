package com.github.nill14.parsers.dependency;

public class DependencyBuildException extends Exception {
	
	
	private static final long serialVersionUID = -1559542704169025088L;

	private final IDependencyCollector module;
	private final String dependency;

	public DependencyBuildException(IDependencyCollector module, String dependency) {
		super(String.format("%s misses required dependency %s", module.getName(), dependency));
		this.module = module;
		this.dependency = dependency;;
	}
	
	public IDependencyCollector getModule() {
		return module;
	}
	
	public String getDependency() {
		return dependency;
	}

}
