package controllers;

import helpers.Graph;
import helpers.GraphData;
import helpers.Statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.overview;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;

public class Overview extends Controller {

  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    Filter filter = Application.getFilterFromSession();

    Statistics stats = null;
    GraphData data = null;
    if (filter != null) {
      BasicDBObject ref = new BasicDBObject("descriminator", filter.getDescriminator());
      DBCursor cursor = Configurator.getDefaultConfigurator().getPersistence().find(Constants.TBL_FILTERS, ref);

      Logger.info("filter is not null");
      if (cursor.count() == 1) { // only root filter
        Logger.info("filter has no parent, using cached statistics");
        // used cached results
        stats = FilterController.getCollectionStatistics(filter.getCollection());
        data = Overview.getDefaultGraphs(filter, true);
      } else {
        // calculate new results
        stats = FilterController.getCollectionStatistics(filter);
        data = Overview.getDefaultGraphs(filter, false);
      }

    }
    return ok(overview.render(names, data, stats));
  }

  private static GraphData getDefaultGraphs(Filter f, boolean root) {
    List<Graph> graphs = new ArrayList<Graph>();
    for (String prop : Application.PROPS) {
      Graph graph;
      if (root) {
        graph = FilterController.getGraph(f.getCollection(), prop);
      } else {
        graph = FilterController.getGraph(f, prop);
      }

      graph.sort();
      
      if (graph.getKeys().size() > 100) {
        graph.cutLongTail();
      }
      graphs.add(graph);

      // TODO decide when to cut long tail...
    }

    return new GraphData(graphs);
  }

}
