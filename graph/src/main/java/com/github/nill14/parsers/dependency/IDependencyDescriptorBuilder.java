package com.github.nill14.parsers.dependency;


public interface IDependencyDescriptorBuilder<K> {

	/**
	 * consumes, dependsOn 
	 * @param dependency a service provider 
	 * @return self
	 */
	IDependencyDescriptorBuilder<K> uses(K dependency);

	/**
	 * consumes, dependsOn (optionally)
	 * @param dependency a service provider 
	 * @return self
	 */
	IDependencyDescriptorBuilder<K> usesOptionally(K dependency);

	/**
	 * provides or isPrerequisiteOf
	 * @param service a service provider 
	 * @return self
	 */
	IDependencyDescriptorBuilder<K> provides(K service);

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
	IDependencyDescriptorBuilder<K> executionPriority(int priority);
	
	IDependencyDescriptor<K> build();

}