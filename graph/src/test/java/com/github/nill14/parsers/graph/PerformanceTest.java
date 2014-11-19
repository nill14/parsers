package com.github.nill14.parsers.graph;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.IConsumer;
import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.impl.DependencyGraphFactory;
import com.github.nill14.parsers.dependency.impl.ModuleRankingsPrinter;
import com.github.nill14.parsers.graph.utils.GraphWalker1;
import com.github.nill14.parsers.graph.utils.GraphWalker2;
import com.github.nill14.parsers.graph.utils.GraphWalker3;
import com.github.nill14.parsers.graph.utils.GraphWalker4;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PerformanceTest {
	
	private static final Logger log = LoggerFactory.getLogger(PerformanceTest.class);
	private static Set<Module> modules;
	private static IDependencyGraph<Module> dependencyGraph;
	private static DirectedGraph<Module, GraphEdge<Module>> graph;
	private static ImmutableList<Module> topologicalOrder;
	private static Map<Module, Integer> moduleRankings;
	private static final int parallelism = Runtime.getRuntime().availableProcessors();
//	private static final int parallelism = 20;
	private static final ExecutorService executor = Executors.newFixedThreadPool(parallelism);

	private static Set<Module> buildChain(String prefix, int count, int padding) {
		Set<Module> set = Sets.newHashSet();
		Module module = Module.builder(prefix, 1).buildModule();
		set.add(module);
		String prev = module.getName();
		
		for (int i = 2; i <= count; i++ ) {
			module = Module.builder(prefix, i).uses(prev).buildModule();
			set.add(module);
			prev = module.getName();
		}
		
		for (int i = count +1; i <= count + padding; i++ ) {
			set.add(Module.builder(prefix, i).buildModule());
		}
		
		return set;
	}

	
	private static final IConsumer<Module> consumer = new IConsumer<Module>() {
		@Override
		public void process(Module module) throws Exception {
			String p = module.getPrefix();
			int c = module.getCounter();
			if (p.startsWith("A") && c == 1) {
				Thread.sleep(500);
			} else if (p.startsWith("B") && c == 10) {
				Thread.sleep(300);
			}
			else {
				Thread.sleep(1);
			}
			synchronized (this) {
				log.info("Executing module {}", module);
			}
		}
	};
	private static ImmutableMap<String,Module> index;



	@BeforeClass
	public static void init() throws CyclicGraphException, UnsatisfiedDependencyException {
		Set<Module> modules = Sets.newHashSet();
		
		modules.addAll(buildChain("A", 1000, 10));
		modules.addAll(buildChain("B", 1000, 10));
		modules.addAll(buildChain("C", 1000, 10));
		modules.addAll(buildChain("D", 1000, 10));
		modules.addAll(buildChain("E", 1000, 10));
		modules.addAll(buildChain("F", 1000, 10));
		modules.addAll(buildChain("G", 1000, 10));
		modules.addAll(buildChain("H", 1000, 10));
		modules.addAll(buildChain("I", 1000, 10));
		modules.addAll(buildChain("J", 1000, 10));
		
		PerformanceTest.modules = modules;
		dependencyGraph = DependencyGraphFactory.newInstance(modules, Module.adapterFunction);
		graph = dependencyGraph.getGraph();
		topologicalOrder = ImmutableList.copyOf(dependencyGraph.getTopologicalOrder());
		moduleRankings = dependencyGraph.getModuleRankings();
		index = Maps.uniqueIndex(modules, new Function<Module, String>() {

			@Override
			public String apply(Module input) {
				return input.getPrefix() + "-" + input.getCounter();
			}
		});
	}
	
	@Test
	public void createDependencyGraph() throws UnsatisfiedDependencyException, CyclicGraphException {
		DependencyGraphFactory.newInstance(modules, Module.adapterFunction);
	}

	@Test
	public void createGraph() throws UnsatisfiedDependencyException {
		DependencyGraphFactory.newGraph(modules, Module.adapterFunction);
	}

	@Test
	public void testDependencies()  {

		Module moduleA = index.get("A-1000");
		
		assertEquals(999, dependencyGraph.getAllDependencies(moduleA).size());
		assertEquals(999, dependencyGraph.getAllDependencies(moduleA).size());
	}
	
	
	public GraphWalker<Module> createGraphWalker2() throws UnsatisfiedDependencyException, CyclicGraphException {
		
		DirectedGraph<Module, GraphEdge<Module>> graph = dependencyGraph.getGraph();
		ImmutableList<Module> topologicalOrder = ImmutableList.copyOf(dependencyGraph.getTopologicalOrder());
		Map<Module, Integer> moduleRankings = dependencyGraph.getModuleRankings();
		return new GraphWalker2<>(graph, topologicalOrder, moduleRankings, parallelism);
	}
	
	@Test
	public void testWalk() throws InterruptedException, ExecutionException {
		dependencyGraph.walkGraph(executor, consumer);
	}
	
	@Test
	public void testWalker2() throws InterruptedException, ExecutionException, UnsatisfiedDependencyException, CyclicGraphException {
		final GraphWalker<Module> graphWalker = new GraphWalker2<>(graph, topologicalOrder, moduleRankings, parallelism);
		walk(graphWalker);
	}

	@Test
	public void testWalker4() throws InterruptedException, ExecutionException, UnsatisfiedDependencyException, CyclicGraphException {
		final GraphWalker<Module> graphWalker = new GraphWalker4<>(graph, topologicalOrder, moduleRankings, parallelism);
		walk(graphWalker);
	}

	
	@Test
	public void rawParallelExecution() throws Exception {
		/* ignore order and just execute everything.
		 * this measures time of the execution without any dependencies
		 * and it helps to identify the eventual walking overhead.
		 * For 4 threads the execution time is equal with walker's
		 * For 20 threads the walker takes double time (1.8x)
		 */
		final Semaphore parallelism = new Semaphore(PerformanceTest.parallelism);
		for (final Module module : topologicalOrder) {
			parallelism.acquire();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						consumer.process(module);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					parallelism.release();
				}
			});
		}
	}
	
	@Test
	public void testWalker1() throws InterruptedException, ExecutionException {
		final GraphWalker<Module> graphWalker = new GraphWalker1<>(graph, topologicalOrder, parallelism);
		walk(graphWalker);
	}
	
	@Test
	public void testWalker3() throws InterruptedException, ExecutionException {
		final GraphWalker<Module> graphWalker = new GraphWalker3<>(graph, topologicalOrder, parallelism);
		walk(graphWalker);
	}

	private void walk(final GraphWalker<Module> graphWalker)
			throws ExecutionException {
		
		for (int i = 0; i < graphWalker.size(); i++) {
			final Module module = graphWalker.releaseNext();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						consumer.process(module);
						graphWalker.onComplete(module);
					} catch (Exception e) {
						graphWalker.onFailure(module, e);
					}
				}
			});
		}
		graphWalker.awaitCompletion();
	}	
	
//	@Test
	public void testWalkSynchronously() throws InterruptedException, ExecutionException {
		dependencyGraph.iterateTopoOrder(consumer);
	}	
	
	@Test
	public void testModuleRankings() {
		new ModuleRankingsPrinter<>(dependencyGraph).toInfoLog(log);
	}	
	
	
}
