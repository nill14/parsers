package com.github.nill14.parsers.dependency.impl;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

public class DependencyTreePrinter<M> {
	
	private final DirectedGraph<M, GraphEdge<M>> graph;
	private final Map<M, Integer> moduleRatings;
	private final List<M> topologicalOrder;
	private final boolean filterTransitive;
	private final IDependencyGraph<M> dependencyGraph;

	public DependencyTreePrinter(IDependencyGraph<M> dependencyGraph) {
		this(dependencyGraph, true);
	}
	
	public DependencyTreePrinter(IDependencyGraph<M> dependencyGraph, boolean filterTransitive) {
		this.dependencyGraph = dependencyGraph;
		this.filterTransitive = filterTransitive;
		this.graph = dependencyGraph.getGraph();
		moduleRatings = dependencyGraph.getModuleRankings();
		
		topologicalOrder = dependencyGraph.getTopologicalOrder();
	}
	
	private Collection<M> findRootNodes() {
		return FluentIterable.from(topologicalOrder).filter(new Predicate<M>() {
			@Override
			public boolean apply(M vertex) {
				return !graph.hasSucccessors(vertex);
			}
		}).toList();
	}
	
	private void visitRootNode(StringConsumer lineConsumer, M vertex, Set<M> visited) {
		printLine(lineConsumer, vertex, "", "");
		visitNode(lineConsumer, "", vertex, visited);
	}
	
	private void visitNode(StringConsumer lineConsumer, String prefix, M vertex, Set<M> visited) {
		Set<M> predecessors = dependencyGraph.getDirectDependencies(vertex);
		if (filterTransitive && !predecessors.isEmpty() && visited.contains(vertex)) {
			int count = dependencyGraph.getAllDependencies(vertex).size();
			if (count > 1) {
				// print skipped line when we can just print the line is overkill
				printSkippedLine(lineConsumer, prefix, count);
			} else {
				printLine(lineConsumer, predecessors.iterator().next(), prefix, " \\- ");
			}
		} else {
			visited.add(vertex);
			int count = 0;
			for (M n : predecessors) {
				boolean last = ++count == predecessors.size();
				if (last) {
					printLine(lineConsumer, n, prefix, " \\- ");
					visitNode(lineConsumer, prefix + "   ", n, visited);
				} else {
					printLine(lineConsumer, n, prefix, " +- ");
					visitNode(lineConsumer, prefix + " | ", n, visited);
				}
			}
		}
	}
	
	private void printLine(StringConsumer lineConsumer, M vertex, String prefix, String next) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append(next);
	
		int rating = moduleRatings.get(vertex);
		b.append(String.format("%s (%d)", vertex, rating));
		try {
			lineConsumer.process(b.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void printSkippedLine(StringConsumer lineConsumer, String prefix, int count) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append(" \\- ");
	
		b.append(String.format("... (skipped %d other dependencies)", count));
		try {
			lineConsumer.process(b.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void processLines(StringConsumer lineConsumer) {
		Set<M> visited = Sets.newHashSet();
		Collection<M> rootNodes = findRootNodes();
		for (M rootNode : rootNodes) {
			visitRootNode(lineConsumer, rootNode, visited);
		}
	}
	
	/**
	 * Outputs dependency tree to System.out
	 */
	public void toConsole() {
		toPrintStream(System.out);
	}
	
	/**
	 * Outputs the dependency tree to a PrintStream
	 */
	public void toPrintStream(final PrintStream p) {
		p.println("Dependency tree");
		processLines(new PrintStreamConsumer(p));
	}
	
	/**
	 * Outputs dependency tree to {@link Logger#info(String)}
	 */
	public void toInfoLog(final Logger log) {
		if (log.isInfoEnabled()) {
			log.info("Dependency tree");
			processLines(new InfoLogConsumer(log));
		}
	}
	
	/**
	 * Outputs dependency tree to {@link Logger#debug(String)}
	 */
	public void toDebugLog(final Logger log) {
		if (log.isDebugEnabled()) {
			log.debug("Dependency tree");
			processLines(new DebugLogConsumer(log));
		}
	}

	/**
	 * @deprecated use {@link #toInfoLog(Logger)} instead
	 */
	@Deprecated
	public void toInfoLog() {
		toInfoLog(LoggerFactory.getLogger(DependencyTreePrinter.class));
	}
	
	/**
	 * @deprecated use {@link #toDebugLog(Logger)} instead
	 */
	@Deprecated
	public void toDebugLog() {
		toDebugLog(LoggerFactory.getLogger(DependencyTreePrinter.class));
	}
}
