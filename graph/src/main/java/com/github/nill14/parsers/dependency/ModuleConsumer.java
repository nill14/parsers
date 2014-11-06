package com.github.nill14.parsers.dependency;

public interface ModuleConsumer<T> {

	/**
	 * Perform an arbitrary code with the module.
	 * Execution is guaranteed to be performed in topological order.
	 * @param module The module to process
	 * @throws Exception any processing exception
	 */
	void process(T module) throws Exception;
	
}
