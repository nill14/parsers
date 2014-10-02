package graph;

public interface GraphEdge<T> {

  GraphNode source();
  GraphNode target();
  T evaluation();
}
