package com.github.nill14.parsers.graph;

import java.util.Set;

import com.github.nill14.parsers.dependency.DependencyCollector;
import com.github.nill14.parsers.dependency.IDependencyCollector;
import com.github.nill14.parsers.dependency.IDependencyCollectorBuilder;

public class Module implements IDependencyCollector<String> {
	
	private final IDependencyCollector<String> collector;
	
	private Module(Builder builder) {
		collector = builder.builder.build();
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

	public static Builder builder(String name) {
		return new Builder(name);
	}
	
	@Override
	public String toString() {
		return collector.toString();
	}
	
	public static class Builder implements IDependencyCollectorBuilder<String> {
		
		private final IDependencyCollectorBuilder<String> builder;
		
		public Builder(String name) {
			builder = DependencyCollector.builder(name);
		}
		
		public Builder dependsOn(String fqn) {
			builder.dependsOn(fqn);
			return this;
		}

		public Builder dependsOnOptionally(String fqn) {
			builder.dependsOnOptionally(fqn);
			return this;
		}
		
		
		public Builder provides(String fqn) {
			builder.provides(fqn);
			return this;
		}		
		
		public Module build() {
			return new Module(this);
		}

	}

}
