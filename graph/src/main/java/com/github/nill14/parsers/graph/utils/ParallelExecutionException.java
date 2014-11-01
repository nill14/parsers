package com.github.nill14.parsers.graph.utils;

import java.util.Set;

import com.google.common.collect.Sets;

public class ParallelExecutionException extends Exception {


	private static final long serialVersionUID = 7428517529378492238L;
	private final Exception failure;
	private final Set<Exception> failures;

	public ParallelExecutionException(Exception failure) {
		super(failure);
		this.failure = failure;
		failures = Sets.newHashSet(failure);
	}
	
	public ParallelExecutionException(Set<Exception> failures) {
		this.failures = failures;
		failure = failures.iterator().next();
	}

	public Exception getFailure() {
		return failure;
	}
	
	public Set<Exception> getFailures() {
		return failures;
	}
}
