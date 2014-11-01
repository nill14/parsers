package graph;

import com.google.common.collect.ImmutableList;

public class Module {
	
	
	private final ImmutableList<String> dependencies;
	private final ImmutableList<String> dependants;
	private final String name;

	private Module(Builder builder) {
		dependencies = builder.dependencies.build();
		dependants = builder.dependants.build();
		name = builder.name;
	}

	
	public ImmutableList<String> getDependants() {
		return dependants;
	}
	
	public ImmutableList<String> getDependencies() {
		return dependencies;
	}

	public static Builder builder(String name) {
		return new Builder(name);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public static class Builder {
		
		private final ImmutableList.Builder<String> dependencies = ImmutableList.builder();
		private final ImmutableList.Builder<String> dependants = ImmutableList.builder();
		private final String name;
		
		public Builder(String name) {
			this.name = name;
		}

		public Builder consumes(String fqn) {
			//dependsOn
			dependencies.add(fqn);
			return this;
		}
		
		public Builder produces(String fqn) {
			//isPrerequisiteOf
			dependants.add(fqn);
			return this;
		}
		
		public Module build() {
			return new Module(this);
		}
	}

}
