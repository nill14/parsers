package com.github.nill14.parsers.dependency.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.nill14.parsers.dependency.IDependencyGraph;
import com.github.nill14.parsers.graph.DirectedGraph;
import com.github.nill14.parsers.graph.GraphEdge;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;

public class DependencyTree<M> {
	
	private static final Logger log = LoggerFactory.getLogger(DependencyTree.class);

	private final DirectedGraph<M, GraphEdge<M>> graph;
	private final Map<M, Integer> moduleRatings;
	private final SetMultimap<M, M> directDependencies = HashMultimap.create();
	private final SetMultimap<M, M> transitiveDependencies = HashMultimap.create();
	private final List<M> topologicalOrder;

	public DependencyTree(IDependencyGraph<M> dependencyGraph) {
		//by default, do not filter indirect dependencies
		//filtering produces little bit shorter list but may be confusing
		this(dependencyGraph, false);
	}
	
	public DependencyTree(IDependencyGraph<M> dependencyGraph, boolean filterTransitive) {
		this.graph = dependencyGraph.getGraph();
		moduleRatings = dependencyGraph.getModuleRatings();
		
		topologicalOrder = dependencyGraph.getTopologicalOrder();
		
		for (M node : topologicalOrder) {
			Set<M> dependencies = graph.predecessors(node);
			directDependencies.putAll(node, dependencies);
			for (M dependency : dependencies) {
				transitiveDependencies.putAll(node, directDependencies.get(dependency));
				transitiveDependencies.putAll(node, transitiveDependencies.get(dependency));
			}
		}
		
		if (filterTransitive) {
			for (M node : topologicalOrder) {
				Set<M> direct = directDependencies.get(node);
				Set<M> transitive = transitiveDependencies.get(node);
				direct.removeAll(transitive);
			}
		}
	}
	
	private Collection<M> findRootNodes() {
		return FluentIterable.from(topologicalOrder).filter(new Predicate<M>() {
			@Override
			public boolean apply(M vertex) {
				return !graph.hasSucccessors(vertex);
			}
		}).toSet();
	}
	
	private void visitRootNode(Collection<String> result, M vertex) {
		result.add(printLine(vertex, "", ""));
		visitNode(result, "", vertex);
	}
	
	private void visitNode(Collection<String> result, String prefix, M vertex) {
		Set<M> predecessors = directDependencies.get(vertex);
		int count = 0;
		for (M n : predecessors) {
			boolean last = ++count == predecessors.size();
			if (last) {
				result.add(printLine(n, prefix, " \\- "));
				visitNode(result, prefix + "   ", n);
			} else {
				result.add(printLine(n, prefix, " +- "));
				visitNode(result, prefix + " | ", n);
			}
		}
	}
	
	private String printLine(M vertex, String prefix, String next) {
		StringBuilder b = new StringBuilder();
		b.append(prefix);
		b.append(next);
	
		int rating = moduleRatings.get(vertex);
		b.append(String.format("%s (%d)", vertex, rating));
		return b.toString();
	}
	
	public Collection<String> getLines() {
		Collection<String> result = Lists.newArrayList();
		Collection<M> rootNodes = findRootNodes();
		for (M rootNode : rootNodes) {
			visitRootNode(result, rootNode);
		}
		return result;
	}
	
	public String toString() {
		return "Dependency tree\n" + Joiner.on("\n").join(getLines());
	}
	
	public void toInfoLog() {
		log.info("Dependency tree");
		for (String line : getLines()) {
			log.info(line);
		}
	}
	
	public void toDebugLog() {
		log.debug("Dependency tree");
		for (String line : getLines()) {
			log.debug(line);
		}
	}
	
}
