Dependency Graph
================

Dependency Graph Library is an implementation of parallel initialization framework.
Modules are initialized in parallel fashion with prerequisite modules being executed first.
Using a graph algorithm is the most effective solution for the parallelization.
Actual time savings depend of graph structure and could vary
but according on our experience the actual savings in real scenarios usually exceed 50% for the parallelized parts.


Dependencies
------------

 * Java 1.7 - We actually use <code>Throwable#addSuppressed(Throwable)</code> introduced in Java 1.7
 * Guava - Google Collections library (Recommended 18.0)
 * Slf4j-api - Logging (Recommended 1.7.7)
 

Minimal example
---------------
<pre><code>
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
Set<AbstractModule> modules = Sets.newHashSet(new ModuleA(), new ModuleB(), new ModuleC());

IDependencyGraph<AbstractModule> dependencyGraph = 
	DependencyGraphFactory.newInstance(modules, m -> m.getDependencyDescriptor());

ExecutorService executor = Executors.newCachedThreadPool();
// execute first ModuleA and ModuleC in parallel and when completed, executes ModuleB
dependencyGraph.walkGraph(executor, module -> System.out.println(module));

// prints out the dependency tree to System.out
new DependencyTreePrinter<>(dependencyGraph).toConsole();

}
}
</code></pre>

Exception propagation
---------------------

The exceptions which might occur during the parallel execution are being propagated back to the caller thread.
The actual exceptions (from all executor threads) are wrapped into a single `java.util.concurrent.ExecutionException`.
Uncaught exception from a worker thread means the execution will be interrupted and remaining modules will be skipped.
Due to parallel nature it might happen that some more modules begin with execution. If they fail too, 
the exception is added to the originating `java.util.concurrent.ExecutionException` as a suppressed exception.

<pre><code>
try {
	dependencyGraph.walkGraph(executor, new IConsumer<Module>() {
		@Override
		public void process(Module module) throws Exception {
			//exception from worker thread	
			throw new MyException();
		}
	});
} catch (ExecutionException e) {
	assert(e.getCause() instanceof MyException);
}	
</code></pre>


Maven dependency
---------------

<pre><code>
&lt;dependencies&gt;
	&lt;dependency&gt;
		&lt;groupId&gt;com.github.nill14.parsers&lt;/groupId&gt;
		&lt;artifactId&gt;graph&lt;/artifactId&gt;
		&lt;version&gt;2.1.1&lt;/version&gt;
	&lt;/dependency&gt;
&lt;/dependencies&gt;
</code></pre>


Performance tests
-----------------
For performance tests, see `com.github.nill14.parsers.graph.PerformanceTest`.
The test creates little over 10000 modules and executes them using given parallelism value. 
The raw execution (ignoring dependency order) is compared to one of the actual 
`com.github.nill14.parsers.graph.GraphWalker` implementations. By raw execution code, we mean Thread.sleep(1) code.

 * For 4 threads, the GraphWalker time is equal to raw execution (!)
 * For 20 threads, the GraphWalker time was about a double of the raw execution.
 
There are actually 4 implementations of `com.github.nill14.parsers.graph.GraphWalker` interface, 
each providing slightly different characteristics for different graph structures and parallelism levels.
Should be performance a critical factor for your application, you may want to experiment with different GraphWalker implementations.   

 