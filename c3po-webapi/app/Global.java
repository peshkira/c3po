import java.util.List;

import play.Application;
import play.GlobalSettings;
import play.Logger;

import com.mongodb.DBCollection;
import com.mongodb.MapReduceCommand.OutputType;
import com.petpet.c3po.analysis.mapreduce.HistogrammJob;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class Global extends GlobalSettings {

  @Override
  public void onStart(Application app) {
    Logger.info("Starting c3po web app");
    super.onStart(app);

    Configurator.getDefaultConfigurator().configure();
    this.calculateCollectionStatistics();
    this.calculateHistogramms();
  }

  // TODO think of a better way to decide when to drop the
  // mapreduce results.
  @Override
  public void onStop(Application app) {
    Logger.info("Stopping c3po web app");
    super.onStop(app);
  }

  private void calculateCollectionStatistics() {
    Logger.info("Calculating size statistics of each collection");
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    final Property size = pl.getCache().getProperty("size");
    final List<String> names = controllers.Application.getCollectionNames();

    for (String name : names) {
      final String cName = "statistics_" + name;
      final DBCollection c = pl.getDB().getCollection(cName);

      if (c.find().count() == 0) {
        Logger.info("No statistics found for collection " + name + ", rebuilding");

        final NumericAggregationJob job = new NumericAggregationJob(name, size);
        job.setType(OutputType.REPLACE);
        job.setOutputCollection(cName);
        job.execute();
      }
    }
  }

  private void calculateHistogramms() {
    Logger.info("Calculating histograms of each collection");
    final List<String> names = controllers.Application.getCollectionNames();
    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();

    for (String name : names) {
      for (String p : controllers.Application.PROPS) {
        final String cName = "histogram_" + name + "_" + p;
        final DBCollection c = pl.getDB().getCollection(cName);

        if (c.find().count() == 0) {
          Logger.info("No histogram found for collection " + name + "  and property " + p + ", rebuilding");

          final HistogrammJob job = new HistogrammJob(name, p);
          job.setType(OutputType.REPLACE);
          job.setOutputCollection(cName);
          job.execute();
        }
      }
    }
  }

}
