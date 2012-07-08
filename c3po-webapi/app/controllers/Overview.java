package controllers;

import helpers.Graph;
import helpers.GraphData;
import helpers.Statistics;

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
      
      final Graph mimes;
      final Graph formats;
      final Graph versions;
      final Graph valid;
      final Graph wf;
      final Graph creatingapp;
      Logger.info("filter is not null");
      if (cursor.count() == 1) { //only root filter
        Logger.info("filter has no parent, using cached statistics");
        // used cached results
        stats = FilterController.getCollectionStatistics(filter.getCollection());
        mimes = FilterController.getGraph(filter.getCollection(), "mimetype");
        formats = FilterController.getGraph(filter.getCollection(), "format");
        versions = FilterController.getGraph(filter.getCollection(), "format_version");
        valid = FilterController.getGraph(filter.getCollection(), "valid");
        wf = FilterController.getGraph(filter.getCollection(), "wellformed");
        creatingapp = FilterController.getGraph(filter.getCollection(), "creating_application_name");

      } else {
        // calculate new results
        Logger.info("filter has parent, calculating statisticts");
        stats = FilterController.getCollectionStatistics(filter);
        mimes = FilterController.getGraph(filter, "mimetype");
        formats = FilterController.getGraph(filter, "format");
        versions = FilterController.getGraph(filter, "format_version");
        valid = FilterController.getGraph(filter, "valid");
        wf = FilterController.getGraph(filter, "wellformed");
        creatingapp = FilterController.getGraph(filter, "creating_application_name");
      }

      mimes.sort();
      formats.sort();
      versions.sort();
//      valid.convertToPercentage();
      valid.sort();
//      wf.convertToPercentage();
      wf.sort();
      creatingapp.sort();
      creatingapp.cutLongTail();

      data = new GraphData(Arrays.asList(mimes, formats, versions, valid, wf, creatingapp));
    }
    return ok(overview.render(names, data, stats));
  }

}
