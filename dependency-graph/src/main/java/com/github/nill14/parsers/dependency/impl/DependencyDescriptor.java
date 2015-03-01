package com.github.nill14.parsers.dependency.impl;

import java.util.Set;

import com.github.nill14.parsers.dependency.IDependencyDescriptor;
import com.github.nill14.parsers.dependency.IDependencyDescriptorBuilder;
import com.google.common.collect.ImmutableSet;

public final class DependencyDescriptor<K> implements IDependencyDescriptor<K> {
	
	
	private final ImmutableSet<K> dependencies;
	private final ImmutableSet<K> optDependencies;
	private final ImmutableSet<K> optProviders;
	private final K self;
	private final int priority;

	private DependencyDescriptor(Builder<K> builder) {
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
	public int getExecutionPriority() {
		return priority;
	}
	
	public static <K> IDependencyDescriptorBuilder<K> builder(K self) {
		return new Builder<>(self);
	}
	
	
	@Override
	public String toString() {
		return self.toString();
	}
	
	public static class Builder<K> implements IDependencyDescriptorBuilder<K> {
		
		private final ImmutableSet.Builder<K> dependencies = ImmutableSet.builder();
		private final ImmutableSet.Builder<K> optDependencies = ImmutableSet.builder();
		private final ImmutableSet.Builder<K> providers = ImmutableSet.builder();
		private final K self;
		private int priority = 0;
		
		public Builder(K name) {
			this.self = name;
			providers.add(name);
		}
		
		@Override
		public IDependencyDescriptorBuilder<K> uses(K dependency) {
			dependencies.add(dependency);
			return this;
		}
		
		@Override
		public IDependencyDescriptorBuilder<K> usesOptionally(K dependency) {
			optDependencies.add(dependency);
			return this;
		}
		
		@Override
		public IDependencyDescriptorBuilder<K> provides(K provider) {
			providers.add(provider);
			return this;
		}
		
		@Override
		public IDependencyDescriptorBuilder<K> executionPriority(int priority) {
			this.priority = priority;
			return this;
		}

		
		@Override
		public IDependencyDescriptor<K> build() {
			return new DependencyDescriptor<>(this);
		}
	}

}
