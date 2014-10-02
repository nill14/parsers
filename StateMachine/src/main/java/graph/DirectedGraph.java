package graph;

import java.util.Set;

public interface DirectedGraph<T> {

  Set<GraphNode> nodes();
  Set<GraphEdge<T>> edges();
  
  DirectedGraph<T> inverse();
  
  Set<GraphEdge<T>> edges(GraphNode node);
  
}
