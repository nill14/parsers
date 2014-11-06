package com.github.nill14.parsers.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.DependencyBuildException;
import com.github.nill14.parsers.dependency.DependencyBuilder;
import com.github.nill14.parsers.dependency.IDependencyManager;
import com.github.nill14.parsers.dependency.ModuleConsumer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphWalkerTest {
	
	private static final Logger log = LoggerFactory.getLogger(GraphWalkerTest.class);
	private final ExecutorService executor = Executors.newFixedThreadPool(8);
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private DependencyBuilder<Module> dependencyBuilder;
	private IDependencyManager<Module> walker; 
	private ImmutableMap<String, Module> moduleIndex;


	@Rule public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() throws CyclicGraphException, DependencyBuildException {
		modules = ImmutableSet.of(
			Module.builder("A")
				.produces("A")
				.consumes("M")
				.build(),
			Module.builder("B")
				.consumes("A")
				.build(),
			Module.builder("C")
				.consumes("A")
				.consumes("B")
				.build(),	
				
			//not connected	
			Module.builder("D")
				.build(),
				
			Module.builder("E")
				.build(),	
			Module.builder("F")
				.consumes("E")
				.build(),
			Module.builder("G")
				.consumes("F")
				.build(),
				
			Module.builder("H")
				.consumes("C")
				.build(),
			Module.builder("I")
				.consumes("C")
				.build(),
			Module.builder("J")
				.produces("A")
				.build(),
				
			Module.builder("K")
				.build(),
			Module.builder("L")
				.consumes("K")
				.build(),
				
			Module.builder("M")
				.build()
		);		
		
		dependencyBuilder = new DependencyBuilder<>(modules);
		walker = dependencyBuilder.buildManager();
		graph = dependencyBuilder.getGraph();
		
		moduleIndex = Maps.uniqueIndex(modules, new Function<Module, String>() {

			@Override
			public String apply(Module input) {
				return input.getName();
			}
		});
	}
	
	public Module findModule(String fqn) {
		return moduleIndex.get(fqn);
	}

	public void assertDependency(String a, String b) {
		Module nodeA = findModule(a);
		Module nodeB = findModule(b);
		
		assertTrue(nodeA + "->" + nodeB, graph.successors(nodeA).contains(nodeB));
		assertTrue(nodeA + "->" + nodeB, graph.predecessors(nodeB).contains(nodeA));
	}

	private void assertTopoOrder(List<Module> topologicalOrdering) {
		log.info("{}", topologicalOrdering);
		assertEquals(modules.size(), topologicalOrdering.size());
		for (int i = 0; i < topologicalOrdering.size() - 1; i++) {
			Module n = topologicalOrdering.get(i);
			for (int j = i; j < topologicalOrdering.size(); j++) {
				Module m = topologicalOrdering.get(j);
				
				assertFalse(n + "<-" + m, graph.predecessors(n).contains(m));
			}
		}
	}
	
	@Test
	public void testWalk() throws InterruptedException, ExecutionException {
		final AtomicInteger count = new AtomicInteger();
		final Queue<Module> executionOrder = new ConcurrentLinkedQueue<>();
		
		walker.walkGraph(executor, new ModuleConsumer<Module>() {
			
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
		
		walker.iterateTopoOrder(new ModuleConsumer<Module>() {
			
			@Override
			public void process(Module module) throws Exception {
				log.info("Starting module {}", module);
				log.info("Completing module {}", module);
				count.incrementAndGet();
				executionOrder.add(module);
			}
		});
		
		assertEquals(modules.size(), count.get());
		assertEquals(walker.getTopologicalOrder(), Lists.newArrayList(executionOrder));
	}	
	
	
	@Test
	public void testException() throws InterruptedException, IOException {
		final AtomicInteger count = new AtomicInteger();
		
		thrown.expect(IOException.class);
		thrown.expectMessage("test checked exception");
		
		try {
			walker.walkGraph(executor, new ModuleConsumer<Module>() {
				
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
		log.info(walker.getDependencyHierarchy());
	}

	@Test
	public void testExhaustException() throws InterruptedException, IOException {
		final AtomicInteger count = new AtomicInteger();
		
		thrown.expect(IOException.class);
		thrown.expectMessage("test checked exception");
		
		try {
			walker.walkGraph(executor, new ModuleConsumer<Module>() {
				
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
	
}
