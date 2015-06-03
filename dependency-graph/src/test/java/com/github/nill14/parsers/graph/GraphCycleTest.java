package com.github.nill14.parsers.graph;
import static org.testng.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.impl.DependencyGraphFactory;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class GraphCycleTest {
	
	private static final Logger log = LoggerFactory.getLogger(GraphCycleTest.class);
	
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private ImmutableMap<String, Module> moduleIndex;



	@BeforeMethod
	public void init() throws UnsatisfiedDependencyException, CyclicGraphException {
		modules = ImmutableSet.of(
			Module.builder("A")
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
				
			//cycle	
			Module.builder("E")
				.usesOptionally("G")
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
				.usesOptionally("C")
				.buildModule(),
			Module.builder("J")
				.provides("A")
				.buildModule(),
				
			// another cycle	
			Module.builder("K")
				.uses("L")
				.buildModule(),
			Module.builder("L")
				.uses("K")
				.buildModule(),
				
			Module.builder("M")
				.uses("H")
				.buildModule()
		);		

		graph = DependencyGraphFactory.newGraph(modules, Module.adapterFunction);
		
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

	@Test
	public void testCyclicException() {
		try {
			DependencyGraphFactory.fromGraph(graph, Collections.<Module, Integer>emptyMap());
		} catch (UnsatisfiedDependencyException e) {
			e.printStackTrace();
		} catch (CyclicGraphException e) {
			log.info("{}", e.getDebugCycles());
		}
		Collection<Deque<Module>> cycles = new GraphCycleDetector<>(graph).getNontrivialCycles();
		
		
		assertEquals(3, cycles.size());
	}
}
