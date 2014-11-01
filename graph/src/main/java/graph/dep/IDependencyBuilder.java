package graph.dep;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.github.nill14.parsers.graph.CyclicGraphException;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.github.nill14.parsers.graph.utils.ParallelExecutionException;

public interface IDependencyBuilder<Module extends IDependencyCollector> {

	/**
	 * In the graph a dependency relation is modeled by precursors direction
	 * That is, when sorted topologically, the most left element doesn't depend on anything. 
	 * The foremost right element depends on it's precursors
	 * @return directed (possibly acyclic) graph
	 */
	DirectedGraph<Module, GraphEdge<Module>> getGraph();

	Set<Module> getCollectors();
	
	List<Module> getTopologicalOrder() throws CyclicGraphException;
	
	Collection<Deque<Module>> getCycles();
	
	void walkGraph(ExecutorService executor, ModuleConsumer<Module> moduleConsumer) throws ParallelExecutionException;

	/**
	 * Synchronous version
	 */
	@Deprecated
	void walkGraph(ModuleConsumer<Module> moduleConsumer) throws ParallelExecutionException;
}