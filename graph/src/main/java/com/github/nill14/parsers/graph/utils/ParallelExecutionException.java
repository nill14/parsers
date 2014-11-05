package com.github.nill14.parsers.graph.utils;


public class ParallelExecutionException extends Exception {


	private static final long serialVersionUID = 7428517529378492238L;
	private final Exception failure;

	public ParallelExecutionException(Exception failure) {
		super(failure);
		this.failure = failure;
	}
	
	public Exception getFailure() {
		return failure;
	}
	
}
