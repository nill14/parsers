package com.github.nill14.parsers.graph;

import java.util.Set;

import com.github.nill14.parsers.dependency.IModule;
import com.github.nill14.parsers.dependency.IModuleDependencyBuilder;
import com.github.nill14.parsers.dependency.impl.DependencyModule;

public class Module implements IModule<String> {
	
	private final IModule<String> collector;
	
	private Module(Builder builder) {
		collector = builder.builder.build();
	}

	public IModule<String> getCollector() {
		return collector;
	}
	
	@Override
	public Set<String> getRequiredDependencies() {
		return collector.getRequiredDependencies();
	}

	@Override
	public Set<String> getOptionalDependencies() {
		return collector.getOptionalDependencies();
	}
	
	@Override
	public Set<String> getOptionalProviders() {
		return collector.getOptionalProviders();
	}
	
	@Override
	public int getModulePriority() {
		return collector.getModulePriority();
	}
	
	public static Builder builder(String name) {
		return new Builder(name);
	}
	
	@Override
	public String toString() {
		return collector.toString();
	}
	
	public static class Builder implements IModuleDependencyBuilder<String> {
		
		private final IModuleDependencyBuilder<String> builder;
		
		public Builder(String name) {
			builder = DependencyModule.builder(name);
		}
		
		@Override
		public Builder dependsOn(String fqn) {
			builder.dependsOn(fqn);
			return this;
		}

		@Override
		public Builder dependsOnOptionally(String fqn) {
			builder.dependsOnOptionally(fqn);
			return this;
		}
		
		@Override
		public Builder provides(String fqn) {
			builder.provides(fqn);
			return this;
		}		

		@Override
		public IModuleDependencyBuilder<String> modulePriority(int priority) {
			builder.modulePriority(priority);
			return this;
		}
		
		@Override
		public IModule<String> build() {
			return builder.build();
		}

		public Module buildModule() {
			return new Module(this);
		}
		
	}

}
