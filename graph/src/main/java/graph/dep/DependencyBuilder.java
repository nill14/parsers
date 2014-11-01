package graph.dep;

import graph.impl.DefaultDirectedGraph;
import graph.impl.EvaluatedGraphEdge;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.utils.GraphCycleDetector;
import com.github.nill14.parsers.graph.utils.GraphWalker;
import com.github.nill14.parsers.graph.utils.LongestPathTopoSorter;
import com.github.nill14.parsers.graph.utils.ParallelExecutionException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

public class DependencyBuilder<Module extends IDependencyCollector> implements IDependencyBuilder<Module> {

	private final Set<Module> modules;
	private final DirectedGraph<Module, GraphEdge<Module>> graph;

	public DependencyBuilder(Set<Module> collectors) {
		modules = collectors;

		ImmutableSetMultimap.Builder<String, Module> consumersBuilder = ImmutableSetMultimap.builder();
		ImmutableSetMultimap.Builder<String, Module> producersBuilder = ImmutableSetMultimap.builder();
		
		for (Module node : collectors) {
			for (String consumer : node.getDependencies()) {
				consumersBuilder.put(consumer, node);
			}
			
			for (String producer : node.getProviders()) {
				producersBuilder.put(producer, node);
			}
		}
		
		SetMultimap<String, Module> consumers = consumersBuilder.build();
		SetMultimap<String, Module> producers = producersBuilder.build();
		Builder<GraphEdge<Module>> edges = ImmutableSet.builder();
		
		Set<String> keys = Sets.union(consumers.keySet(), producers.keySet());
		for (String key : keys) {
			Set<Module> from = producers.get(key);
			Set<Module> to = consumers.get(key);
			
			for (Module source : from) {
				for (Module target : to) {
					GraphEdge<Module> edge = EvaluatedGraphEdge.edge(source, target);
					System.out.println(source + "->" + target);
					edges.add(edge);
				}
			}
		}
		
		graph = DefaultDirectedGraph.<Module, GraphEdge<Module>>builder()
			.nodes(collectors)
			.edges(edges.build())
			.build();
	}
	
	
	@Override
	public DirectedGraph<Module, GraphEdge<Module>> getGraph() {
		return graph;
	}
	
	@Override
	public Set<Module> getCollectors() {
		return modules;
	}
	
	@Override
	public Collection<Deque<Module>> getCycles() {
		return new GraphCycleDetector<>(graph).getNontrivialCycles();
	}
	
	@Override
	public List<Module> getTopologicalOrder() throws CyclicGraphException {
		return new LongestPathTopoSorter<>(graph).getLongestPathTopologicalOrdering();
	}
	
	@Override
	public void walkGraph(final ExecutorService executor,
			final ModuleConsumer<Module> moduleConsumer)
			throws ParallelExecutionException {
		
		final GraphWalker<Module> graphWalker = new GraphWalker<>(graph);
		
		for (final Module module : graphWalker) {
			graphWalker.checkFailure();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						moduleConsumer.process(module);
						graphWalker.onComplete(module);
					} catch (Exception e) {
						graphWalker.onFailure(e);
					}
				}
			});
		}
		graphWalker.awaitCompletion();
	}
	
	@Override
	public void walkGraph(ModuleConsumer<Module> moduleConsumer) throws ParallelExecutionException {
		GraphWalker<Module> graphWalker = new GraphWalker<>(graph);
		
		for (Module module : graphWalker) {
			try {
				moduleConsumer.process(module);
				graphWalker.onComplete(module);
			} catch (Exception e) {
				throw new ParallelExecutionException(e);
			}
		}
		
	}
	
}
