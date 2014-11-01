package graph;

import graph.dep.DependencyCollector;
import graph.dep.IDependencyCollector;

import java.util.List;

public class Module implements IDependencyCollector {
	
	private final IDependencyCollector collector;
	
	private Module(Builder builder) {
		collector = builder.builder.build();
	}

	@Override
	public List<String> getDependencies() {
		return collector.getDependencies();
	}
	
	@Override
	public List<String> getProviders() {
		return collector.getProviders();
	}

	public static Builder builder(String name) {
		return new Builder(name);
	}
	
	@Override
	public String getName() {
		return collector.getName();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public static class Builder {
		
		private final graph.dep.DependencyCollector.Builder builder;
		
		public Builder(String name) {
			builder = DependencyCollector.builder(name);
		}
		
		public Builder dependsOn(Class<?> clazz) {
			builder.dependsOn(clazz);
			return this;
		}
		
		public Builder isPrerequisiteOf(Class<?> clazz) {
			builder.isPrerequisiteOf(clazz);
			return this;
		}

		public Builder dependsOn(String fqn) {
			builder.dependsOn(fqn);
			return this;
		}
		
		public Builder isPrerequisiteOf(String fqn) {
			builder.isPrerequisiteOf(fqn);
			return this;
		}
		

		public Builder consumes(String fqn) {
			builder.dependsOn(fqn);
			return this;
		}
		
		public Builder produces(String fqn) {
			builder.isPrerequisiteOf(fqn);
			return this;
		}		
		
		public Module build() {
			return new Module(this);
		}
	}

}
