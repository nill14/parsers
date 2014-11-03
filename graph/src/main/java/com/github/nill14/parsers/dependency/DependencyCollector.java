package com.github.nill14.parsers.dependency;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class DependencyCollector implements IDependencyCollector {
	
	
	private final ImmutableSet<String> dependencies;
	private final ImmutableSet<String> optDependencies;
	private final ImmutableSet<String> optProviders;
	private final String name;

	private DependencyCollector(Builder builder) {
		dependencies = builder.dependencies.build();
		optDependencies = builder.optDependencies.build();
		optProviders = builder.providers.build();
		name = builder.name;
	}

	@Override
	public Set<String> getRequiredDependencies() {
		return dependencies;
	}
	
	@Override
	public Set<String> getOptionalDependencies() {
		return optDependencies;
	}
	
	@Override
	public Set<String> getOptionalProviders() {
		return optProviders;
	}

	public static Builder builder(String name) {
		return new Builder(name);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public static class Builder {
		
		private final ImmutableSet.Builder<String> dependencies = ImmutableSet.builder();
		private final ImmutableSet.Builder<String> optDependencies = ImmutableSet.builder();
		private final ImmutableSet.Builder<String> providers = ImmutableSet.builder();
		private final String name;
		
		public Builder(String name) {
			this.name = name;
			providers.add(name);
		}
		
		/**
		 * consumes, dependsOn 
		 * @param clazz a service provider 
		 * @return self
		 */
		public Builder dependsOn(Class<?> clazz) {
			return dependsOn(clazz.getName());
		}
		
		/**
		 * consumes, dependsOn (optionally)
		 * @param clazz a service provider 
		 * @return self
		 */
		public Builder dependsOnOptionally(Class<?> clazz) {
			return dependsOnOptionally(clazz.getName());
		}
		
		/**
		 * provides or isPrerequisiteOf
		 * @param clazz a service provider 
		 * @return self
		 */
		public Builder provides(Class<?> clazz) {
			return provides(clazz.getName());
		}

		public Builder dependsOn(String fqn) {
			dependencies.add(fqn);
			return this;
		}
		
		public Builder dependsOnOptionally(String fqn) {
			optDependencies.add(fqn);
			return this;
		}
		
		public Builder provides(String fqn) {
			providers.add(fqn);
			return this;
		}
		
		public IDependencyCollector build() {
			return new DependencyCollector(this);
		}
	}

}
