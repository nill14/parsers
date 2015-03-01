package com.github.nill14.parsers.graph;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.impl.DependencyGraphFactory;
import com.github.nill14.parsers.graph.utils.LongestPathTopoSorter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class GraphCycleSelfUseTest {
	
	private static final Logger log = LoggerFactory.getLogger(GraphCycleSelfUseTest.class);
	
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private ImmutableMap<String, Module> moduleIndex;



	@Before
	public void init() throws UnsatisfiedDependencyException, CyclicGraphException {
		modules = ImmutableSet.of(
				Module.builder("A")
					.uses("M")
					.provides("M")
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
		
		assertTrue(nodeA + "->" + nodeB, graph.successors(nodeA).contains(nodeB));
		assertTrue(nodeA + "->" + nodeB, graph.predecessors(nodeB).contains(nodeA));
	}
	
	
	@Test
	public void testLongestPath() throws CyclicGraphException {
		new LongestPathTopoSorter<>(graph).getLongestPathTopologicalOrdering();
		
		
	}

}
