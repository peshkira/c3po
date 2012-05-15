package com.petpet.c3po.controller;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.petpet.c3po.adaptor.fits.FITSDigesterAdaptor;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.gatherer.FileSystemGatherer;

public class Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
  private FITSDigesterAdaptor fits;
  private DB db;
  
  public Controller(DB db) {
    this.db = db;
    this.fits = new FITSDigesterAdaptor();
  }

  public void collect(Map<String, String> config) {

    FileSystemGatherer gatherer = new FileSystemGatherer(config);

    if (gatherer != null) {
      LOGGER.info("Found matching gatherer of type, starting...");
      LOGGER.info("{} files to be processed", gatherer.getCount());

      if (gatherer.getCount() > 100) {
        int counter = 10;
        List<InputStream> next = gatherer.getNext(10);

        while (!next.isEmpty()) {
          LOGGER.debug("processing next {} files", next.size());

          this.dispatch(next);
          next = gatherer.getNext(10);
          counter += 10;
 
          if (counter % 500 == 0) {
            // cleanUp();
            LOGGER.info("Finished processing {} files", counter);
          }
        }

      } else {
        List<InputStream> all = gatherer.getAll();
        this.dispatch(all);
        // cleanUp();
      }
    }

    LOGGER.info("Gathering process finished");
  }

  private void dispatch(List<InputStream> list) {
    for (InputStream is : list) {
      fits.setStream(is);
      Element e = fits.getElement();

      this.processElement(e);
    }
  }

  public synchronized void processElement(Element e) {
    DBCollection elements = db.getCollection("elements");
    BasicDBObject document = e.getDocument();
//    LOGGER.info("Storing:\n{}", document.toString());
    elements.insert(document);
    
  }

}
