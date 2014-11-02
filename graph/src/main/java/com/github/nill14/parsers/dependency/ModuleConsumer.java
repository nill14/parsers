package com.github.nill14.parsers.dependency;

public interface ModuleConsumer<Module extends IDependencyCollector> {

	void process(Module module) throws Exception;
	
}
