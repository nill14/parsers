package com.github.nill14.parsers.dependency;

public interface ModuleConsumer<T> {

	void process(T module) throws Exception;
	
}
