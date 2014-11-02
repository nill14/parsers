package graph;

import java.util.Set;

import com.github.nill14.parsers.dependency.DependencyCollector;
import com.github.nill14.parsers.dependency.IDependencyCollector;

public class Module implements IDependencyCollector {
	
	private final IDependencyCollector collector;
	
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
	public String getName() {
		return collector.getName();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public static class Builder {
		
		private final com.github.nill14.parsers.dependency.DependencyCollector.Builder builder;
		
		public Builder(String name) {
			builder = DependencyCollector.builder(name);
		}
		
		public Builder consumes(String fqn) {
			builder.dependsOn(fqn);
			return this;
		}

		public Builder consumesOpt(String fqn) {
			builder.dependsOnOptionally(fqn);
			return this;
		}
		
		
		public Builder produces(String fqn) {
			builder.provides(fqn);
			return this;
		}		
		
		public Module build() {
			return new Module(this);
		}
	}

}
