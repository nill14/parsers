package com.github.nill14.parsers.graph;

import com.github.nill14.parsers.dependency.IModuleDependencyDescriptor;
import com.github.nill14.parsers.dependency.IModuleDependencyBuilder;
import com.github.nill14.parsers.dependency.impl.ModuleDependencyDescriptor;
import com.google.common.base.Function;

public class Module {
	
	private final IModuleDependencyDescriptor<String> collector;
	
	private Module(Builder builder) {
		collector = builder.builder.build();
	}

	public IModuleDependencyDescriptor<String> getCollector() {
		return collector;
	}
	
	public static Builder builder(String name) {
		return new Builder(name);
	}
	
	@Override
	public String toString() {
		return collector.toString();
	}
	
	public static final Function<Module, IModuleDependencyDescriptor<String>> adapterFunction = new Function<Module, IModuleDependencyDescriptor<String>>() {
		
		@Override
		public IModuleDependencyDescriptor<String> apply(Module input) {
			return input.getCollector();
		}
	};
	
	public static class Builder implements IModuleDependencyBuilder<String> {
		
		private final IModuleDependencyBuilder<String> builder;
		
		public Builder(String name) {
			builder = ModuleDependencyDescriptor.builder(name);
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
		public IModuleDependencyDescriptor<String> build() {
			return builder.build();
		}

		public Module buildModule() {
			return new Module(this);
		}
		
	}

}
