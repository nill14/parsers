package com.github.nill14.parsers.dependency;

import java.util.Set;

/**
 * 
 *
 * K is an arbitrary type but provides correct {@link Object#hashCode()} and {@link Object#equals(Object)}
 */
public interface IModule<K> {

	
	/**
	 * a dependsOn(string) or consumes(string)
	 * @return a set of dependencies	
	 */
	Set<K> getRequiredDependencies();
	
	/**
	 * a dependsOn(string) or consumes(string)
	 * @return a set of optional dependencies	
	 */
	Set<K> getOptionalDependencies();

	/**
	 * All providers (inversive dependency) are optional. 
	 * Nobody is forced to consume the service.
	 * 
	 * a isPrerequisiteOf(string) or produces(string)
	 * @return a set of providers
	 */
	Set<K> getOptionalProviders();
	
	int getModulePriority();

}

