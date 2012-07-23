package controllers;

import java.util.ArrayList;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.samples;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.petpet.c3po.analysis.SizeRepresentativeGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

public class Samples extends Controller {

  public static Result index() {
    final List<String> names = Application.getCollectionNames();
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    final Filter filter = Application.getFilterFromSession();
    final SizeRepresentativeGenerator sg = new SizeRepresentativeGenerator();
    sg.setFilter(filter);

    List<Element> result = new ArrayList<Element>();
    if (filter != null) {
      List<String> output = sg.execute();
      BasicDBObject query = new BasicDBObject("uid", new BasicDBObject("$in", output));
      DBCursor cursor = pl.find(Constants.TBL_ELEMENTS, query);
      System.out.println(cursor.count());
      while (cursor.hasNext()) {
        result.add(DataHelper.parseElement(cursor.next(), pl));
      }
    }

    return ok(samples.render(names, result));
  }

}
