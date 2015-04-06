package com.github.nill14.parsers.graph;

import static org.testng.Assert.*;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.dependency.UnsatisfiedDependencyException;
import com.github.nill14.parsers.dependency.impl.DependencyGraphFactory;
import com.github.nill14.parsers.dependency.impl.DependencyTreePrinter;
import com.github.nill14.parsers.dependency.impl.ModuleRankingsPrinter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class DependencyTreePrinterTest {
	
	private static final Logger log = LoggerFactory.getLogger(DependencyTreePrinterTest.class);
	
	private static DirectedGraph<Module, GraphEdge<Module>> graph;
	private static Set<Module> modules;
	private static IDependencyGraph<Module> dependencyGraph;
	private static ImmutableMap<String, Module> moduleIndex;


	@BeforeClass
	public static void init() throws CyclicGraphException, UnsatisfiedDependencyException {
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
	public void testDependencyTreeShort() {
		new DependencyTreePrinter<>(dependencyGraph, true).toInfoLog(log);;
	}

	@Test
	public void testDependencyTreeLong() {
		new DependencyTreePrinter<>(dependencyGraph, false).toInfoLog(log);
	}
	
	@Test
	public void testModuleRankings() {
		new ModuleRankingsPrinter<>(dependencyGraph).toInfoLog(log);
	}	
}
