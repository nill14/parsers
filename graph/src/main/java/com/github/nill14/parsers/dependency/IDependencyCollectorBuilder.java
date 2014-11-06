package com.github.nill14.parsers.dependency;


public interface IDependencyCollectorBuilder<K> {

	/**
	 * consumes, dependsOn 
	 * @param T a service provider 
	 * @return self
	 */
	IDependencyCollectorBuilder<K> dependsOn(K dependency);

	/**
	 * consumes, dependsOn (optionally)
	 * @param T a service provider 
	 * @return self
	 */
	IDependencyCollectorBuilder<K> dependsOnOptionally(K dependency);

	/**
	 * provides or isPrerequisiteOf
	 * @param T a service provider 
	 * @return self
	 */
	IDependencyCollectorBuilder<K> provides(K service);

	IDependencyCollector<K> build();

}