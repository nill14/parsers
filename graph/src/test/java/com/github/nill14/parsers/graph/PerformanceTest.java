package com.github.nill14.parsers.graph;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.IConsumer;
import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.impl.DependencyGraphFactory;
import com.github.nill14.parsers.graph.utils.GraphWalkerLegacy;
import com.github.nill14.parsers.graph.utils.GraphWalker1;
import com.github.nill14.parsers.graph.utils.GraphWalker2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class PerformanceTest {
	
	private static final Logger log = LoggerFactory.getLogger(PerformanceTest.class);
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private Set<Module> modules;
	private IDependencyGraph<Module> dependencyGraph;
	private GraphWalker<Module> graphWalker;

	private Set<Module> buildChain(String prefix, int count) {
		Set<Module> set = Sets.newHashSet();
		String prev = prefix + "-1";
		set.add(Module.builder(prev).buildModule());
		
		for (int i = 2; i <= count; i++ ) {
			String curr = prefix + "-" + i;
			set.add(Module.builder(curr).uses(prev).buildModule());
			prev = curr;
		}
		return set;
	}
	
	private static final IConsumer<Module> consumer = new IConsumer<Module>() {
		@Override
		public void process(Module module) throws Exception {
			Thread.sleep(1);
			synchronized (this) {
				log.info("Executing module {}", module);
			}
		}
	};



	@Before
	public void init() throws CyclicGraphException, UnsatisfiedDependencyException {
		Set<Module> modules = Sets.newHashSet();
		
		modules.addAll(buildChain("A", 1000));
		modules.addAll(buildChain("B", 1000));
		modules.addAll(buildChain("C", 1000));
		modules.addAll(buildChain("D", 1000));
		modules.addAll(buildChain("E", 1000));
		modules.addAll(buildChain("F", 1000));
		modules.addAll(buildChain("G", 1000));
		modules.addAll(buildChain("H", 1000));
		modules.addAll(buildChain("I", 1000));
		modules.addAll(buildChain("J", 1000));
		
		this.modules = modules;
		createDependencyGraph();
		createGraphWalker2();
	}
	
	@Test
	public void createDependencyGraph() throws UnsatisfiedDependencyException, CyclicGraphException {
		dependencyGraph = DependencyGraphFactory.newInstance(modules, Module.adapterFunction);
	}
	
	@Test
	public void createGraphWalker2() throws UnsatisfiedDependencyException, CyclicGraphException {
		
		DirectedGraph<Module, GraphEdge<Module>> graph = dependencyGraph.getGraph();
		ImmutableList<Module> topologicalOrder = ImmutableList.copyOf(dependencyGraph.getTopologicalOrder());
		Map<Module, Integer> moduleRankings = dependencyGraph.getModuleRankings();
		graphWalker = new GraphWalker2<>(graph, topologicalOrder, moduleRankings);
	}
	
	@Test
	public void testRankings() {
		for (Entry<Module, Integer> entry : dependencyGraph.getModuleRankings().entrySet()) {
			log.debug("{}", entry);
		}
	}


	@Test
	public void testWalk() throws InterruptedException, ExecutionException {
		dependencyGraph.walkGraph(executor, consumer);
	}
	
	@Test
	public void testWalker2Complete() throws InterruptedException, ExecutionException, UnsatisfiedDependencyException, CyclicGraphException {
		createGraphWalker2();
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
	
	@Test
	public void testWalker2Only() throws InterruptedException, ExecutionException {
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
	
	@Test
	public void testWalkerOnly() throws InterruptedException, ExecutionException {
		GraphWalkerLegacy<Module> graphWalker = new GraphWalkerLegacy<>(dependencyGraph.getGraph(), dependencyGraph.getTopologicalOrder());
		try {
			for (Module module : graphWalker) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							consumer.process(module);
							graphWalker.onComplete(module);
						} catch (Exception e) {
							graphWalker.onFailure(e);
						}
					}
				});
			}
			graphWalker.awaitCompletion();
			
		} catch (NoSuchElementException e) {
			graphWalker.checkFailure();
		}
	}
	
	@Test
	public void testWalker1Only() throws InterruptedException, ExecutionException {
		GraphWalker<Module> graphWalker = new GraphWalker1<>(dependencyGraph.getGraph(), dependencyGraph.getTopologicalOrder());
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
	
	@Test
	public void testWalkSynchronously() throws InterruptedException, ExecutionException {
		dependencyGraph.iterateTopoOrder(consumer);
	}	
	
	
}
