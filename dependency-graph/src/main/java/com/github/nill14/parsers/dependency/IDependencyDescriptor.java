package com.github.nill14.parsers.dependency;

import java.util.Set;

/**
 * 
 *
 * K is an arbitrary type but provides correct {@link Object#hashCode()} and {@link Object#equals(Object)}
 */
public interface IDependencyDescriptor<K> {

	
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
	
	/**
	 * Module priority lifts up the module in execution order
	 * as long as dependencies are satisfied.
	 * Module rating is based on self-priority and priority of follow-up modules.
	 * Dependency module have always higher rating than dependant module.
	 * Rating is calculated as maximum or path cost and module priority.
	 * 
	 * 
	 * @return module priority
	 */
	int getExecutionPriority();

}

