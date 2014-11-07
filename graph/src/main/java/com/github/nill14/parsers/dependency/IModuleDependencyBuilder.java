package com.github.nill14.parsers.dependency;


public interface IModuleDependencyBuilder<K> {

	/**
	 * consumes, dependsOn 
	 * @param dependency a service provider 
	 * @return self
	 */
	IModuleDependencyBuilder<K> dependsOn(K dependency);

	/**
	 * consumes, dependsOn (optionally)
	 * @param dependency a service provider 
	 * @return self
	 */
	IModuleDependencyBuilder<K> dependsOnOptionally(K dependency);

	/**
	 * provides or isPrerequisiteOf
	 * @param service a service provider 
	 * @return self
	 */
	IModuleDependencyBuilder<K> provides(K service);

	IModuleDependencyBuilder<K> modulePriority(int priority);
	
	IModule<K> build();

}