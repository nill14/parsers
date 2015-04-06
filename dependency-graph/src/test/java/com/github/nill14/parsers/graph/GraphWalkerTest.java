package com.github.nill14.parsers.graph;
import static org.testng.Assert.*;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.nill14.parsers.dependency.IConsumer;
import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.impl.DependencyGraphFactory;
import com.github.nill14.parsers.dependency.impl.DependencyTreePrinter;
import com.github.nill14.parsers.dependency.impl.ModuleRankingsPrinter;
import com.github.nill14.parsers.graph.utils.GraphWalker3;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphWalkerTest {
	
	private static final Logger log = LoggerFactory.getLogger(GraphWalkerTest.class);
	private final ExecutorService executor = Executors.newFixedThreadPool(8);
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private IDependencyGraph<Module> dependencyGraph;
	private ImmutableMap<String, Module> moduleIndex;


	private ImmutableList<Module> topoList;

	@BeforeMethod
	public void init() throws CyclicGraphException, UnsatisfiedDependencyException {
		modules = ImmutableSet.of(
			Module.builder("A")
				.provides("A")
				.uses("M")
				.buildModule(),
			Module.builder("B")
				.uses("A")
				.buildModule(),
			Module.builder("C")
				.uses("A")
				.uses("B")
				.buildModule(),	
				
			//not connected	
			Module.builder("D")
				.buildModule(),
				
			Module.builder("E")
				.buildModule(),	
			Module.builder("F")
				.uses("E")
				.buildModule(),
			Module.builder("G")
				.uses("F")
				.buildModule(),
				
			Module.builder("H")
				.uses("C")
				.buildModule(),
			Module.builder("I")
				.uses("C")
				.buildModule(),
			Module.builder("J")
				.provides("A")
				.buildModule(),
				
			Module.builder("K")
				.buildModule(),
			Module.builder("L")
				.uses("K")
				.buildModule(),
				
			Module.builder("M")
				.buildModule()
		);		
		
		dependencyGraph = DependencyGraphFactory.newInstance(modules, Module.adapterFunction);
		graph = dependencyGraph.getGraph();
		topoList = ImmutableList.copyOf(dependencyGraph.getTopologicalOrder());
		
		moduleIndex = Maps.uniqueIndex(modules, new Function<Module, String>() {

			@Override
			public String apply(Module input) {
				return input.toString();
			}
		});
	}
	
	public Module findModule(String fqn) {
		return moduleIndex.get(fqn);
	}

	public void assertDependency(String a, String b) {
		Module nodeA = findModule(a);
		Module nodeB = findModule(b);
		
		assertTrue(graph.successors(nodeA).contains(nodeB), nodeA + "->" + nodeB);
		assertTrue(graph.predecessors(nodeB).contains(nodeA), nodeA + "->" + nodeB);
	}

	private void assertTopoOrder(List<Module> topologicalOrdering) {
		log.info("{}", topologicalOrdering);
		assertEquals(modules.size(), topologicalOrdering.size());
		for (int i = 0; i < topologicalOrdering.size() - 1; i++) {
			Module n = topologicalOrdering.get(i);
			for (int j = i; j < topologicalOrdering.size(); j++) {
				Module m = topologicalOrdering.get(j);
				
				assertFalse(graph.predecessors(n).contains(m), n + "<-" + m);
			}
		}
	}
	
	@Test(timeOut=1000)
	public void testWalk() throws InterruptedException, ExecutionException {
		log.info("testWalk start");
		final AtomicInteger count = new AtomicInteger();
		final Queue<Module> executionOrder = new ConcurrentLinkedQueue<>();
		
		dependencyGraph.walkGraph(executor, new IConsumer<Module>() {
			
			@Override
			public void process(Module module) throws Exception {
				log.info("Starting module {}", module);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				log.info("Completing module {}", module);
				count.incrementAndGet();
				executionOrder.add(module);
			}
		});
		
		assertEquals(modules.size(), count.get());
		assertTopoOrder(Lists.newArrayList(executionOrder));
	}
	
	@Test
	public void testWalkSynchronously() throws InterruptedException, ExecutionException {
		final AtomicInteger count = new AtomicInteger();
		final Queue<Module> executionOrder = new ConcurrentLinkedQueue<>();
		
		dependencyGraph.iterateTopoOrder(new IConsumer<Module>() {
			
			@Override
			public void process(Module module) throws Exception {
				log.info("Starting module {}", module);
				log.info("Completing module {}", module);
				count.incrementAndGet();
				executionOrder.add(module);
			}
		});
		
		assertEquals(modules.size(), count.get());
		assertEquals(dependencyGraph.getTopologicalOrder(), Lists.newArrayList(executionOrder));
	}	
	
	
	@Test(timeOut=1000, expectedExceptions=IOException.class, expectedExceptionsMessageRegExp="test checked exception")
	public void testException() throws InterruptedException, IOException {
		final AtomicInteger count = new AtomicInteger();
		
		try {
			dependencyGraph.walkGraph(executor, new IConsumer<Module>() {
				
				@Override
				public void process(Module module) throws Exception {
					log.info("Starting module {}", module);
					Thread.sleep(100);
					log.info("Completing module {}", module);
					if (count.incrementAndGet() == 5) {
						throw new IOException("test checked exception");
					};
					
				}
			});
		} catch (ExecutionException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw new RuntimeException("Unexpected", e);
			}
		}
		
		assertEquals(modules.size(), count.get());
	}	
	
	@Test
	public void testLog() {
		new ModuleRankingsPrinter<>(dependencyGraph).toInfoLog(log);
		new DependencyTreePrinter<>(dependencyGraph, true).toInfoLog(log);
	}

	@Test(timeOut=1000, expectedExceptions=IOException.class, expectedExceptionsMessageRegExp="test checked exception")
	public void testExhaustException() throws InterruptedException, IOException {
		final AtomicInteger count = new AtomicInteger();
		
		try {
			dependencyGraph.walkGraph(executor, new IConsumer<Module>() {
				
				@Override
				public void process(Module module) throws Exception {
					log.info("Starting module {}", module);
					Thread.sleep(1);
					log.info("Completing module {}", module);
					if (count.incrementAndGet() == 1) {
						//wait until we park with lockCondition
						Thread.sleep(200);
						throw new IOException("test checked exception");
					};
					
				}
			});
		} catch (ExecutionException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw new RuntimeException("Unexpected", e);
			}
		}
		
		assertEquals(modules.size(), count.get());
	}
	
	@Test(timeOut=2000)
	public void testExhaustAndContinue() throws InterruptedException, ExecutionException {
		final AtomicInteger count = new AtomicInteger();
		
		dependencyGraph.walkGraph(executor, new IConsumer<Module>() {
			
			@Override
			public void process(Module module) throws Exception {
				log.info("Starting module {}", module);
				Thread.sleep(1);
				log.info("Completing module {}", module);
				if (count.incrementAndGet() == 1) {
					//wait until we park with lockCondition
					Thread.sleep(200);
				};
			}
		});
		
		assertEquals(modules.size(), count.get());
	}
	
	@Test(timeOut=1000)
	public void testExhaustWalker() throws InterruptedException, IOException, ExecutionException {
		//there are five releaseable modules at the beginning
		final GraphWalker3<Module> walker = new GraphWalker3<>(graph, topoList, 1000);
		final Module next = walker.releaseNext();
		walker.releaseNext();
		walker.releaseNext();
		walker.releaseNext();
		
		walker.releaseNext();
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				walker.onComplete(next);
			}
		});
		Thread.sleep(100);
		walker.releaseNext();
	}
	

	@Test(timeOut=2000)
	public void testExhaustAndContinue2() throws InterruptedException, ExecutionException {
		final AtomicInteger count = new AtomicInteger();
		
		dependencyGraph.walkGraph(executor, new IConsumer<Module>() {
			
			@Override
			public void process(Module module) throws Exception {
				log.info("Starting module {}", module);
				Thread.sleep(5);
				log.info("Completing module {}", module);
				count.incrementAndGet();
			}
		});
		
		assertEquals(modules.size(), count.get());
	}
	
	
	@Test
	public void testDependencies() {
		Module moduleM = findModule("M");
		Module moduleA = findModule("A");
		Module moduleB = findModule("B");
		
		assertTrue(dependencyGraph.getDirectDependencies(moduleA).contains(moduleM));
		assertTrue(!dependencyGraph.getDirectDependencies(moduleB).contains(moduleM));
		assertTrue(dependencyGraph.getAllDependencies(moduleB).contains(moduleM));
	}
}
