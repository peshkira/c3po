
import java.util.List;

import play.Application;
import play.GlobalSettings;
import play.Logger;

import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Configurator;

public class Global extends GlobalSettings {

  @Override
  public void onStart(Application app) {
    Logger.info("Starting c3po web api");
    super.onStart(app);

    Configurator.getDefaultConfigurator().configure();
    this.calculateCollectionStatistics();
  }

  private void calculateCollectionStatistics() {
    Logger.info("Calculating size statistics of each collection");
    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    Property size = pl.getCache().getProperty("size");

    List<String> names = controllers.Application.getCollectionNames();

    for (String name : names) {
      NumericAggregationJob job = new NumericAggregationJob(name, size);
      job.setType(OutputType.REPLACE);
      job.setOutputCollection("statistics_" + name);
      job.execute();
    }
  }

}
