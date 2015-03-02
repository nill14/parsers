package com.github.nill14.parsers.dependency.impl;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.github.nill14.parsers.dependency.IDependencyGraph;

public class ModuleRankingsPrinter<M> {
	
	private final IDependencyGraph<M> dependencyGraph;
	private final Map<M, Integer> moduleRankings;
	private final List<M> topologicalOrder;

	
	public ModuleRankingsPrinter(IDependencyGraph<M> dependencyGraph) {
		this.dependencyGraph = dependencyGraph;
		moduleRankings = dependencyGraph.getModuleRankings();
		topologicalOrder = dependencyGraph.getTopologicalOrder();
	}
	
	
	private String printLine(M vertex, boolean executionBit, int ranking) {
		if (executionBit) {
			return String.format("* %s (%d)", vertex, ranking);
		} else {
			return String.format("  %s (%d)", vertex, ranking);
		}
	}
	
	private void processLines(StringConsumer lineConsumer) {
		for (M node : topologicalOrder) {
			boolean executionBit = dependencyGraph.getDirectDependencies(node).isEmpty();
			int ranking = moduleRankings.get(node);
			lineConsumer.process(printLine(node, executionBit, ranking));
		}
	}
	
	/**
	 * Outputs module rankings to System.out
	 */
	public void toConsole() {
		toPrintStream(System.out);
	}
	
	/**
	 * Outputs module rankings to a PrintStream
	 */
	public void toPrintStream(PrintStream p) {
		p.println("Module Rankings");
		processLines(new PrintStreamConsumer(p));
	}
	
	/**
	 * Outputs module rankings to {@link Logger#info(String)}
	 */
	public void toInfoLog(Logger log) {
		if (log.isInfoEnabled()) {
			log.info("Module Rankings");
			processLines(new InfoLogConsumer(log));
		}
	}
	
	/**
	 * Outputs module rankings to {@link Logger#debug(String)}
	 */
	public void toDebugLog(Logger log) {
		if (log.isDebugEnabled()) {
			log.debug("Module Rankings");
			processLines(new DebugLogConsumer(log));
		}
	}

}
