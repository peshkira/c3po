/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
