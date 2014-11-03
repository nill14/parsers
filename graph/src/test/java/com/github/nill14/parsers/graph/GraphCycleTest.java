package com.github.nill14.parsers.graph;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.DependencyBuildException;
import com.github.nill14.parsers.dependency.DependencyBuilder;
import com.github.nill14.parsers.dependency.IDependencyBuilder;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class GraphCycleTest {
	
	private static final Logger log = LoggerFactory.getLogger(GraphCycleTest.class);
	
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private IDependencyBuilder<Module> dependencyBuilder;
	private ImmutableMap<String, Module> moduleIndex;



	@Before
	public void init() throws DependencyBuildException {
		modules = ImmutableSet.of(
			Module.builder("A")
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
				
			//cycle	
			Module.builder("E")
				.consumesOpt("G")
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
				.consumesOpt("C")
				.build(),
			Module.builder("J")
				.produces("A")
				.build(),
				
			// another cycle	
			Module.builder("K")
				.consumes("L")
				.build(),
			Module.builder("L")
				.consumes("K")
				.build(),
				
			Module.builder("M")
				.consumes("H")
				.build()
		);		

		dependencyBuilder = new DependencyBuilder<>(modules);
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
	
	@Test
	public void testBuild() {
		assertEquals(13, graph.nodes().size());
		assertDependency("A", "B");
		assertDependency("A", "C");
		assertDependency("B", "C");
		assertDependency("M", "A");
		assertDependency("H", "M");
	}
	
	@Test
	public void testCycles() {
		Collection<Deque<Module>> cycles = new GraphCycleDetector<>(graph).getNontrivialCycles();
		
		
		log.info("{}", cycles);
		assertEquals(3, cycles.size());
	}

}
