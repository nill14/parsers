Dependency Graph
================

Dependency Graph Library is an implementation of parallel initialization framework.
Modules are initialized in parallel fashion with prerequisite modules being executed first.
Using a graph algorithm is the most effective solution for the parallelization.
Actual time savings depend of graph structure and could vary
but according on our experience the actual savings in real scenarios usually exceed 50% for the parallelized parts.

JavaDoc
-------

Latest snapshot JavaDoc: [nill14.github.io/parsers/dependency-graph/apidocs/](https://nill14.github.io/parsers/dependency-graph/apidocs/)

Dependencies
------------

 * Java 1.7 - We actually use <code>Throwable#addSuppressed(Throwable)</code> introduced in Java 1.7
 * Guava - Google Collections library (Recommended 18.0)
 * Slf4j-api - Logging (Recommended 1.7.7)
 

Elementary example
------------------

The following code demonstrates minimal yet fully functional example. Just copy the code into your IDE to get started.

<pre><code>
abstract class AbstractModule {
	IDependencyDescriptor&lt;Class&lt;?&gt;&gt; getDependencyDescriptor() {
		IDependencyDescriptorBuilder&lt;Class&lt;?&gt;&gt; builder = 
				DependencyDescriptor.builder(this.getClass());
		buildDependencies(builder);
		return builder.build();
	}

	void buildDependencies(IDependencyDescriptorBuilder&lt;Class&lt;?&gt;&gt; builder) {}
}

class ModuleA extends AbstractModule {}

class ModuleB extends AbstractModule {
	void buildDependencies(IDependencyDescriptorBuilder&lt;Class&lt;?&gt;&gt; builder) {
		builder.uses(ModuleA.class);
		builder.usesOptionally(Calendar.class);
	}
}

class ModuleC extends AbstractModule {
	void buildDependencies(IDependencyDescriptorBuilder&lt;Class&lt;?&gt;&gt; builder) {
		builder.provides(Calendar.class);
	}
}

public class Test {
public static void main(String[] args) throws UnsatisfiedDependencyException, 
	CyclicGraphException, ExecutionException {
	
// create dependency graph
Set&lt;AbstractModule&gt; modules = Sets.newHashSet(new ModuleA(), new ModuleB(), new ModuleC());

IDependencyGraph&lt;AbstractModule&gt; dependencyGraph = 
	DependencyGraphFactory.newInstance(modules, m -&gt; m.getDependencyDescriptor());

ExecutorService executor = Executors.newCachedThreadPool();
// execute first ModuleA and ModuleC in parallel and when completed, executes ModuleB
dependencyGraph.walkGraph(executor, module -&gt; System.out.println(module));

// prints out the dependency tree to System.out
new DependencyTreePrinter&lt;&gt;(dependencyGraph).toConsole();

// prints out the module rankings to System.out
new ModuleRankingsPrinter&lt;&gt;(dependencyGraph).toConsole();
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
	dependencyGraph.walkGraph(executor, new IConsumer&lt;Module&gt;() {
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

 * For 4 threads, the GraphWalker time is equal to raw execution.
 * For 20 threads (on 4 core machine), the execution overhead is about 1s (per 10100 execution units).
 
There are actually 4 implementations of `com.github.nill14.parsers.graph.GraphWalker` interface, 
each providing slightly different characteristics for different graph structures and parallelism levels.
Should performance be a critical factor for your application, you may want to experiment with different GraphWalker implementations.   


Parallelism
-----------

In theory, the optimal thread count equals to number of processor cores.
With low parallelism some cores are not utilized, with high parallelism there occurs context switching
which imposes a performance penalty. `GraphWalker` uses provided `ExecutorService` for submitting releaseable tasks.
Therefore it is recommended to use such an `ExecutorService` which have sufficient amount of worker threads 
and is not loaded with I/O blocking tasks, etc.

<pre><code>
int parallelism = Runtime.getRuntime().availableProcessors();
ExecutorService executor = Executors.newFixedThreadPool(parallelism);
</code></pre>

A word of warning: if you are using directly `ThreadPoolExecutor` and want to tune up corePoolSize or maximumPoolSize,
you may want to read [this article](http://www.bigsoft.co.uk/blog/index.php/2009/11/27/rules-of-a-threadpoolexecutor-pool-size).	

`IDependencyGraph` provides two methods for walking graph. First one determines parallelism automatically 
from provided `ThreadPoolExecutor` and amount of processor cores. The second allows specifying parallelism explicitly.

<pre><code>
IDependencyGraph#walkGraph(executor, moduleConsumer);
IDependencyGraph#walkGraph(executor, moduleConsumer, parallelism);
</code></pre>

`GraphWalker` ensures that no more than `parallelism` tasks are scheduled simultaneously, even when more tasks
are releaseable. By releaseable task is meant such a module that have all dependencies already executed.
Such a scheduling provides faster execution times and ensures that module rankings are respected.
Module ranking is related to chain of dependant modules as a simple predictor of expected execution times. Scheduling modules faster
than they are executed leads to out of order execution and eventually to poor executor utilization at the end of the execution. 

License
-------

The library is available under [Apache License 2.0](http://www.spdx.org/licenses/Apache-2.0).
The license is always specified in pom.xml


 