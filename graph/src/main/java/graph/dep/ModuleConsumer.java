package graph.dep;

public interface ModuleConsumer<Module extends IDependencyCollector> {

	void process(Module module) throws Exception;
	
}
