package graph;

import static org.junit.Assert.*;
import graph.dep.DependencyBuilder;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.github.nill14.parsers.graph.utils.LongestPathTopoSorter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphOrderTest {
	
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;
	private DependencyBuilder<Module> dependencyBuilder;
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
				
			Module.builder("E")
				.produces("E")
				.build(),	
			Module.builder("F")
				.consumes("E")
				.produces("F")
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
				.produces("K")
				.build(),
			Module.builder("L")
				.consumes("K")
				.build(),
				
			Module.builder("M")
				.produces("M")
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
	}
	
	@Test
	public void testNoCycles() {
		Collection<Deque<Module>> cycles = new GraphCycleDetector<>(graph).getNontrivialCycles();
		
		
		System.out.println(cycles);
		assertEquals(0, cycles.size());
	}
	
	@Test
	public void testTopoSort() {
		List<Module> topologicalOrdering = new LongestPathTopoSorter<>(graph).getTopologicalOrdering();
		topoOrder(topologicalOrdering);
	}
	
	@Test
	public void testDepthSort() {
		LinkedHashMap<Module, Integer> topologicalOrdering = new LongestPathTopoSorter<>(graph).getLongestPathMap();
		System.out.println(topologicalOrdering);
		topoOrder(Lists.newArrayList(topologicalOrdering.keySet()));
	}

	private void topoOrder(List<Module> topologicalOrdering) {
		System.out.println(topologicalOrdering);
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
