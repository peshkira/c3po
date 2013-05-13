package com.petpet.c3po.controller;

import java.util.Iterator;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.utils.DataHelper;

public class Consolidator extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(Consolidator.class);

  private static int instance = 0;

  private final Queue<Element> queue;

  private final PersistenceLayer persistence;

  private boolean running;

  public Consolidator(PersistenceLayer p, Queue<Element> q) {
    persistence = p;
    queue = q;
    setName("Consolidator[" + instance++ + "]");
  }

  @Override
  public void run() {
    this.running = true;
    while (this.running || !queue.isEmpty()) {
      try {

        Element e = null;
        synchronized (queue) {

          while (queue.isEmpty()) {

            if (!this.isRunning()) {
              break;
            }

            queue.wait();
          }
          
          //LOG.debug("cons queue count: " + queue.size());
          e = queue.poll();

        }
        // should this be here or outside of the sync block
        process(e);

      } catch (InterruptedException e) {
        LOG.warn("An error occurred in {}: {}", getName(), e.getMessage());
        break;
      }
    }
    LOG.info(getName() + " is stopping");
  }

  private void process(Element element) {
    if (element == null) {
      LOG.debug("Cannot consolidate null element");

      return;
    }

//    LOG.debug(getName() + " is consolidating element: " + element.getUid());
    Filter f = new Filter(new FilterCondition("uid", element.getUid()));
    Iterator<Element> iter = this.persistence.find(Element.class, f);

    if (iter.hasNext()) {
      Element stored = iter.next();

      if (iter.hasNext()) {
        // obviously, there are more than
        // one elements matching the filter
        // so we cannot assume that it already exists
        // and we just store it.
        this.persistence.insert(element);
        return;
      }

      consolidate(element, stored);
      this.persistence.update(stored, f);

    } else {
      this.persistence.insert(element);
    }
    
  }

  private void consolidate(Element element, Element stored) {
    try {

      for (MetadataRecord newMR : element.getMetadata()) {
        DataHelper.mergeMetadataRecord(stored, newMR);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }
}
