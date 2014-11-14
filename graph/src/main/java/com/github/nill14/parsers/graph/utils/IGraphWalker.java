package com.github.nill14.parsers.graph.utils;

import java.util.concurrent.ExecutionException;

public interface IGraphWalker<V> {

	V releaseNext() throws ExecutionException;

	void onComplete(V vertex);

	void onFailure(Exception e);

	boolean isCompleted();
	
	int size();

	void awaitCompletion() throws ExecutionException;

}