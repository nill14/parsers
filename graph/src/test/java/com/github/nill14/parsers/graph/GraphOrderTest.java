package com.github.nill14.parsers.graph;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.impl.DependencyGraphFactory;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.github.nill14.parsers.graph.utils.LongestPathTopoSorter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphOrderTest {
	
	private static final Logger log = LoggerFactory.getLogger(GraphOrderTest.class);
	
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private IDependencyGraph<Module> dependencyBuilder;
	private ImmutableMap<String, Module> moduleIndex;


	@Before
	public void init() throws UnsatisfiedDependencyException, CyclicGraphException {
		modules = ImmutableSet.of(
			Module.builder("A")
				.dependsOnOptionally("M")
				.buildModule(),
			Module.builder("B")
				.dependsOn("A")
				.buildModule(),
			Module.builder("C")
				.dependsOn("A")
				.dependsOn("B")
				.buildModule(),	
				
			//not connected	
			Module.builder("D")
				.buildModule(),
				
			Module.builder("E")
				.buildModule(),	
			Module.builder("F")
				.dependsOn("E")
				.buildModule(),
			Module.builder("G")
				.dependsOn("F")
				.buildModule(),
				
			Module.builder("H")
				.dependsOn("C")
				.buildModule(),
			Module.builder("I")
				.dependsOn("C")
				.buildModule(),
			Module.builder("J")
				.provides("A")
				.buildModule(),
				
			Module.builder("K")
				.buildModule(),
			Module.builder("L")
				.dependsOn("K")
				.buildModule(),
				
			Module.builder("M")
				.buildModule()
		);		
		
		dependencyBuilder = DependencyGraphFactory.newInstance(modules, Module.adapterFunction);
		graph = dependencyBuilder.getGraph();
		
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
	}
	
	@Test
	public void testNoCycles() {
		Collection<Deque<Module>> cycles = new GraphCycleDetector<>(graph).getNontrivialCycles();
		
		
		log.info("{}", cycles);
		assertEquals(0, cycles.size());
	}
	
	@Test
	public void testTopoSort() throws CyclicGraphException {
		List<Module> topologicalOrdering = new LongestPathTopoSorter<>(graph).getTopologicalOrdering();
		assertTopoOrder(topologicalOrdering);
	}
	
	@Test
	public void testDepthSort() throws CyclicGraphException {
		LinkedHashMap<Module, Integer> topologicalOrdering = new LongestPathTopoSorter<>(graph).getLongestPathMap();
		log.info("{}", topologicalOrdering);
		assertTopoOrder(Lists.newArrayList(topologicalOrdering.keySet()));
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
	

}
