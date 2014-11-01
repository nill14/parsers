package graph;

import static org.junit.Assert.*;
import graph.dep.DependencyBuilder;
import graph.dep.IDependencyBuilder;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class GraphCycleTest {
	
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private IDependencyBuilder<Module> dependencyBuilder;
	private ImmutableMap<String, Module> moduleIndex;



	@Before
	public void init() {
		modules = ImmutableSet.of(
			Module.builder("A")
				.produces("A")
				.consumes("M")
				.build(),
			Module.builder("B")
				.consumes("A")
				.produces("B")
				.build(),
			Module.builder("C")
				.consumes("A")
				.consumes("B")
				.produces("C")
				.build(),	
				
			//not connected	
			Module.builder("D")
				.produces("D")
				.build(),
				
			//cycle	
			Module.builder("E")
				.consumes("G")
				.produces("E")
				.build(),	
			Module.builder("F")
				.consumes("E")
				.produces("F")
				.build(),
			Module.builder("G")
				.consumes("F")
				.produces("G")
				.build(),
				
			Module.builder("H")
				.consumes("C")
				.produces("H")
				.build(),
			Module.builder("I")
				.consumes("C")
				.build(),
			Module.builder("J")
				.produces("A")
				.build(),
				
			// another cycle	
			Module.builder("K")
				.produces("K")
				.consumes("L")
				.build(),
			Module.builder("L")
				.produces("L")
				.consumes("K")
				.build(),
				
			Module.builder("M")
				.produces("M")
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
		
		
		System.out.println(cycles);
		assertEquals(3, cycles.size());
	}

}
