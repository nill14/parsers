package graph.dep;

import java.util.List;

public interface IDependencyCollector {

	
	/**
	 * a dependsOn(string) or consumes(string)
	 * @return
	 */
	List<String> getDependencies();

	/**
	 * a isPrerequisiteOf(string) or produces(string)
	 * @return
	 */
	List<String> getProviders();

	String getName();

}

