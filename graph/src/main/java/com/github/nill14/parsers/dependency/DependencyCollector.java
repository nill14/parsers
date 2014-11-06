package com.github.nill14.parsers.dependency;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class DependencyCollector<K> implements IDependencyCollector<K> {
	
	
	private final ImmutableSet<K> dependencies;
	private final ImmutableSet<K> optDependencies;
	private final ImmutableSet<K> optProviders;
	private final K self;

	private DependencyCollector(Builder<K> builder) {
		dependencies = builder.dependencies.build();
		optDependencies = builder.optDependencies.build();
		optProviders = builder.providers.build();
		self = builder.self;
	}

	@Override
	public Set<K> getRequiredDependencies() {
		return dependencies;
	}
	
	@Override
	public Set<K> getOptionalDependencies() {
		return optDependencies;
	}
	
	@Override
	public Set<K> getOptionalProviders() {
		return optProviders;
	}

	public static <K> IDependencyCollectorBuilder<K> builder(K self) {
		return new Builder<>(self);
	}
	
	
	@Override
	public String toString() {
		return self.toString();
	}
	
	public static class Builder<K> implements IDependencyCollectorBuilder<K> {
		
		private final ImmutableSet.Builder<K> dependencies = ImmutableSet.builder();
		private final ImmutableSet.Builder<K> optDependencies = ImmutableSet.builder();
		private final ImmutableSet.Builder<K> providers = ImmutableSet.builder();
		private final K self;
		
		public Builder(K name) {
			this.self = name;
			providers.add(name);
		}
		
		/**
		 * consumes, dependsOn 
		 * @param clazz a service provider 
		 * @return self
		 */
		@Override
		public IDependencyCollectorBuilder<K> dependsOn(K dependency) {
			dependencies.add(dependency);
			return this;
		}
		
		/**
		 * consumes, dependsOn (optionally)
		 * @param clazz a service provider 
		 * @return self
		 */
		@Override
		public IDependencyCollectorBuilder<K> dependsOnOptionally(K dependency) {
			optDependencies.add(dependency);
			return this;
		}
		
		/**
		 * provides or isPrerequisiteOf
		 * @param clazz a service provider 
		 * @return self
		 */
		@Override
		public IDependencyCollectorBuilder<K> provides(K provider) {
			providers.add(provider);
			return this;
		}

		
		@Override
		public IDependencyCollector<K> build() {
			return new DependencyCollector<>(this);
		}
	}

}
