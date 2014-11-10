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

	/**
	 * Module priority lifts up the module in execution order
	 * as long as dependencies are satisfied.
	 * Typical use case might be a splash screen or a logging provider (if not captured by dependencies).
	 * Rating is calculated as maximum of either module priority 
	 * or maximum of path cost and successor priority.
	 * Predecessors (dependencies) have always higher execution rating than this module.
	 * 
	 * @param priority the module priority (0..100000)
	 * @return self
	 */
	IModuleDependencyBuilder<K> modulePriority(int priority);
	
	IModuleDependencyDescriptor<K> build();

}