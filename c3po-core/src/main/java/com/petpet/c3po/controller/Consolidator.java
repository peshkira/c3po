package com.petpet.c3po.controller;

import java.util.Queue;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.model.Element;

//TODO make it possible to stop this thread.
public class Consolidator extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(Consolidator.class);

  private static int instance = 0;

  private final Queue<Element> queue;

  private boolean running;

  public Consolidator(Queue<Element> q) {
    queue = q;
    setName("Consolidator[" + instance++ + "]");
    System.out.println("Created Consolidtor " + getName());
  }

  @Override
  public void run() {
    this.running = true;
    while (this.isRunning() || !queue.isEmpty()) {
      System.out.println(getName() + " run: " + isRunning() + " empty: " + queue.isEmpty());
      try {

        Element e = null;
        synchronized (queue) {

          while (queue.isEmpty()) {

            if (!this.isRunning()) {
              break;
            }

            queue.wait();
          }

          e = queue.poll();
        }

        consolidate(e);

      } catch (InterruptedException e) {
        LOG.warn("An error occurred in {}: {}", getName(), e.getMessage());
        break;
      }
    }
    System.out.println(getName() + "is stopping");
  }

  private void consolidate(Element element) {
    if (element == null) {
      LOG.debug("Cannot consolidate null element");

      return;
    }

    try {
      int mod = (new Random().nextInt(10));
      int millis = Math.abs(mod) * 1000;
      Thread.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println(getName() + " is consolidating element: " + element.getUid());
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    System.out.println("Stop running " + getName());
    this.running = running;
  }
}
