Graph 

Implementation of directed acyclic graph along with some graph algorithms 
and implementation of dependency execution framework.


Typical usage scenario:

abstract class AbstractModule {
	IModuleDependencyDescriptor<Class<?>> getDependencyDescriptor() {
		IModuleDependencyBuilder<Class<?>> builder = 
				ModuleDependencyDescriptor.<Class<?>>builder(this.getClass());
		buildDependencies(builder);
		return builder.build();
	}

	void buildDependencies(IModuleDependencyBuilder<Class<?>> builder) {}
}

class ModuleA extends AbstractModule {}

class ModuleB extends AbstractModule {
	void buildDependencies(IModuleDependencyBuilder<Class<?>> builder) {
		builder.dependsOn(ModuleA.class);
		builder.dependsOnOptionally(Calendar.class);
	}
}

class ModuleC extends AbstractModule {
	void buildDependencies(IModuleDependencyBuilder<Class<?>> builder) {
		builder.provides(Calendar.class);
	}
}

// create dependency graph
Set<AbstractModule> modules = Sets.newHashSet(new ModuleA(), new ModuleB(), new ModuleC());
IDependencyGraph<AbstractModule> dependencyGraph = 
	DependencyGraphFactory.newInstance(modules, m -> m.getDependencyDescriptor());
		
ExecutorService executor = Executors.newCachedThreadPool();
// execute first ModuleA and ModuleC in parallel and when completed, executes ModuleB
dependencyGraph.walkGraph(executor, module -> System.out.println(module));

// prints out dependency tree
System.out.println(new DependencyTreePrinter(dependencyGraph));
