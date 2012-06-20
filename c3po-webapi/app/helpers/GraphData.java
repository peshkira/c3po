package helpers;

import java.util.List;

public class GraphData {

  private List<Graph> graphs;

  public GraphData() {
    
  }
  
  public GraphData(List<Graph> graphs) {
    this.setGraphs(graphs);
  }

  public List<Graph> getGraphs() {
    return graphs;
  }

  public void setGraphs(List<Graph> graphs) {
    this.graphs = graphs;
  }
}
