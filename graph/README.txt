Graph 

Implementation of directed acyclic graph along with some graph algorithms 
and implementation of dependency execution framework.


Typical usage scenario:

abstract class AbstractModule {
	IDependencyDescriptor<Class<?>> getDependencyDescriptor() {
		IDependencyDescriptorBuilder<Class<?>> builder = 
				DependencyDescriptor.builder(this.getClass());
		buildDependencies(builder);
		return builder.build();
	}

	void buildDependencies(IDependencyDescriptorBuilder<Class<?>> builder) {}
}

class ModuleA extends AbstractModule {}

class ModuleB extends AbstractModule {
	void buildDependencies(IDependencyDescriptorBuilder<Class<?>> builder) {
		builder.uses(ModuleA.class);
		builder.usesOptionally(Calendar.class);
	}
}

class ModuleC extends AbstractModule {
	void buildDependencies(IDependencyDescriptorBuilder<Class<?>> builder) {
		builder.provides(Calendar.class);
	}
}

public class Test {
	public static void main(String[] args) throws UnsatisfiedDependencyException, 
			CyclicGraphException, ExecutionException {
		
		// create dependency graph
		Set<AbstractModule> modules = 
			Sets.newHashSet(new ModuleA(), new ModuleB(), new ModuleC());
		
		IDependencyGraph<AbstractModule> dependencyGraph = DependencyGraphFactory
			.newInstance(modules, m -> m.getDependencyDescriptor());
		
		ExecutorService executor = Executors.newCachedThreadPool();
		// execute first ModuleA and ModuleC in parallel and when completed, executes ModuleB
		dependencyGraph.walkGraph(executor, module -> System.out.println(module));
		
		// prints out the dependency tree to System.out
		new DependencyTreePrinter<>(dependencyGraph).toConsole();
	}
}