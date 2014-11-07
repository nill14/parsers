package com.github.nill14.parsers.dependency.impl;

import java.util.Set;

import com.github.nill14.parsers.dependency.IModule;
import com.github.nill14.parsers.dependency.IModuleDependencyBuilder;
import com.google.common.collect.ImmutableSet;

public final class DependencyModule<K> implements IModule<K> {
	
	
	private final ImmutableSet<K> dependencies;
	private final ImmutableSet<K> optDependencies;
	private final ImmutableSet<K> optProviders;
	private final K self;
	private final int priority;

	private DependencyModule(Builder<K> builder) {
		dependencies = builder.dependencies.build();
		optDependencies = builder.optDependencies.build();
		optProviders = builder.providers.build();
		self = builder.self;
		priority = builder.priority;
		
		if (priority < 0 || priority > 100000) {
			throw new IllegalArgumentException("Priority not in range 0..100000: "+ priority);
		}
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

	@Override
	public int getModulePriority() {
		return priority;
	}
	
	public static <K> IModuleDependencyBuilder<K> builder(K self) {
		return new Builder<>(self);
	}
	
	
	@Override
	public String toString() {
		return self.toString();
	}
	
	public static class Builder<K> implements IModuleDependencyBuilder<K> {
		
		private final ImmutableSet.Builder<K> dependencies = ImmutableSet.builder();
		private final ImmutableSet.Builder<K> optDependencies = ImmutableSet.builder();
		private final ImmutableSet.Builder<K> providers = ImmutableSet.builder();
		private final K self;
		private int priority = 0;
		
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
		public IModuleDependencyBuilder<K> dependsOn(K dependency) {
			dependencies.add(dependency);
			return this;
		}
		
		/**
		 * consumes, dependsOn (optionally)
		 * @param clazz a service provider 
		 * @return self
		 */
		@Override
		public IModuleDependencyBuilder<K> dependsOnOptionally(K dependency) {
			optDependencies.add(dependency);
			return this;
		}
		
		/**
		 * provides or isPrerequisiteOf
		 * @param clazz a service provider 
		 * @return self
		 */
		@Override
		public IModuleDependencyBuilder<K> provides(K provider) {
			providers.add(provider);
			return this;
		}
		
		@Override
		public IModuleDependencyBuilder<K> modulePriority(int priority) {
			this.priority = priority;
			return this;
		}

		
		@Override
		public IModule<K> build() {
			return new DependencyModule<>(this);
		}
	}

}
