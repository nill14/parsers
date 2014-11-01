package graph;

import static org.junit.Assert.*;
import graph.impl.DefaultDirectedGraph;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;

public class GraphCycleTest {
	
	private DirectedGraph<Module, GraphEdge<Module>> graph;
	private Set<Module> modules;



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
		
		ImmutableSetMultimap.Builder<String, Module> consumers = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<String, Module> producers = ImmutableSetMultimap.builder();
		
		for (Module node : modules) {
			ImmutableList<String> dependencies = node.getDependencies();
			ImmutableList<String> dependants = node.getDependants();
			
			for (String dependency : dependencies) {
				consumers.put(dependency, node);
			}
			
			for (String dependant : dependants) {
				producers.put(dependant, node);
			}
		}
		
		ImmutableSetMultimap<String, Module> consumers2 = consumers.build();
		ImmutableSetMultimap<String, Module> producers2 = producers.build();
		Builder<GraphEdge<Module>> edges = ImmutableSet.builder();
		
		Set<String> keys = Sets.union(consumers2.keySet(), producers2.keySet());
		for (String key : keys) {
			ImmutableSet<Module> from = producers2.get(key);
			ImmutableSet<Module> to = consumers2.get(key);
			
			for (Module source : from) {
				for (Module target : to) {
					GraphEdge<Module> edge = GraphEdge.newInstance(source, target);
					edges.add(edge);
				}
			}
		}
		
		graph = DefaultDirectedGraph.<Module, GraphEdge<Module>>builder()
			.nodes(modules)
			.edges(edges.build())
			.build();
	}
	
	public Module findModule(String fqn) {
		return modules.stream().filter(m -> m.getName().equals(fqn)).findFirst().get();
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
