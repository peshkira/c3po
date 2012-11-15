package controllers;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.samples;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;
import com.petpet.c3po.analysis.RepresentativeGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

public class Samples extends Controller {

  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    return ok(samples.render(names));
  }

  public static Result getSamples(String alg, int size) {
    Logger.debug("in method getSamples alg " + alg + " size " + size);
    final Configurator configurator = Configurator.getDefaultConfigurator();
    final PersistenceLayer pl = configurator.getPersistence();
    final Filter filter = Application.getFilterFromSession();

    final RepresentativeGenerator sg = new RepresentativeAlgorithmFactory().getAlgorithm(alg);
    sg.setFilter(filter);

    List<Element> samples = new ArrayList<Element>();
    if (filter != null) {
      List<String> output = sg.execute(size);
      BasicDBObject query = new BasicDBObject("uid", new BasicDBObject("$in", output));
      DBCursor cursor = pl.find(Constants.TBL_ELEMENTS, query);
      while (cursor.hasNext()) {
        samples.add(DataHelper.parseElement(cursor.next(), pl));
      }
    }

    return ok(play.libs.Json.toJson(samples));
  }

}
