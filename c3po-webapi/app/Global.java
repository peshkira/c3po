

import com.petpet.c3po.utils.Configurator;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.Play;

public class Global extends GlobalSettings {
  
  @Override
  public void onStart(Application app) {
    Logger.info("Starting c3po web api");
    super.onStart(app);
    
    Configurator.getDefaultConfigurator().configure();
  }

}
