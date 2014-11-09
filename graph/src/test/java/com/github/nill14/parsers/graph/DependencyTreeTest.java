package com.github.nill14.parsers.graph;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.impl.DependencyGraphFactory;
import com.github.nill14.parsers.dependency.impl.DependencyTree;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class DependencyTreeTest {
	
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private IDependencyGraph<Module> dependencyGraph;
	private ImmutableMap<String, Module> moduleIndex;


	@Before
	public void init() throws CyclicGraphException, UnsatisfiedDependencyException {
		modules = ImmutableSet.of(
			Module.builder("A")
				.provides("A")
				.dependsOn("M")
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
		
		dependencyGraph = DependencyGraphFactory.newInstance(modules, Module.adapterFunction);
		graph = dependencyGraph.getGraph();
		
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
	public void testLog() {
		System.out.println(new DependencyTree<>(dependencyGraph, true));
	}

	@Test
	public void testLog2() {
		System.out.println(new DependencyTree<>(dependencyGraph, false));
	}	
}
