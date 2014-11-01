package graph;

import static org.junit.Assert.*;
import graph.impl.DefaultDirectedGraph;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;

public class GraphWalkerTest {
	
	
	private final ExecutorService executor = Executors.newFixedThreadPool(8);
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
	
	@Test
	public void testWalk() throws InterruptedException {
		GraphWalker<Module> walker = new GraphWalker<>(graph);
		AtomicInteger count = new AtomicInteger();
		
		
		for (Module module : walker) {
//		for (Iterator<Module> it = walker.iterator(); it.hasNext();) {
//			final Module module = it.next();
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					System.out.println("Starting module " + module);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("Completing module " + module);
					count.incrementAndGet();
					walker.onComplete(module);
				}
			});
		}
		
		Thread.sleep(200);
		assertEquals(modules.size(), count.get());
	}
	
	@Test
	public void testWalkSynchronously() throws InterruptedException {
		GraphWalker<Module> walker = new GraphWalker<>(graph);
		AtomicInteger count = new AtomicInteger();
		
		
		for (Module module : walker) {
			System.out.println("Starting & completing module " + module);
			count.incrementAndGet();
			walker.onComplete(module);
		}
		
		Thread.sleep(200);
		assertEquals(modules.size(), count.get());
	}	

}
