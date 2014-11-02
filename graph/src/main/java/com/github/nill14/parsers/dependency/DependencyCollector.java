package com.github.nill14.parsers.dependency;

import com.google.common.collect.ImmutableList;

public final class DependencyCollector implements IDependencyCollector {
	
	
	private final ImmutableList<String> dependencies;
	private final ImmutableList<String> providers;
	private final String name;

	private DependencyCollector(Builder builder) {
		dependencies = builder.dependencies.build();
		providers = builder.providers.build();
		name = builder.name;
	}

	@Override
	public ImmutableList<String> getDependencies() {
		return dependencies;
	}
	
	@Override
	public ImmutableList<String> getProviders() {
		return providers;
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
		
		private final ImmutableList.Builder<String> dependencies = ImmutableList.builder();
		private final ImmutableList.Builder<String> providers = ImmutableList.builder();
		private final String name;
		
		public Builder(String name) {
			this.name = name;
		}
		
		public Builder dependsOn(Class<?> clazz) {
			return dependsOn(clazz.getName());
		}
		
		public Builder isPrerequisiteOf(Class<?> clazz) {
			return isPrerequisiteOf(clazz.getName());
		}

		public Builder dependsOn(String fqn) {
			dependencies.add(fqn);
			return this;
		}
		
		public Builder isPrerequisiteOf(String fqn) {
			providers.add(fqn);
			return this;
		}
		
		public IDependencyCollector build() {
			return new DependencyCollector(this);
		}
	}

}
