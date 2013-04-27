package com.petpet.c3po.controller;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.model.Element;

public class Consolidator extends Thread {
  
  private static final Logger LOG = LoggerFactory.getLogger(Consolidator.class);

  private static int instance = 0;
  
  private final Queue<Element> queue;
  
  public Consolidator(Queue<Element> q) {
    queue = q;
    setName("Consolidator[" + instance++ +"]");
    System.out.println("Created Consolidtor " + getName());
  }
  
  @Override
  public void run() {
    
    while (true) {
      try {
        
        synchronized (queue) {
         while (queue.isEmpty()) {
           queue.wait();
         }
         
         Element element = queue.poll();
         consolidate(element);
        }
        
      } catch (InterruptedException e) {
        LOG.warn("An error occurred in {}: {}", getName(), e.getMessage());
        break;
      }
    }
    
  }

  private void consolidate(Element element) {
    if (element == null) {
      LOG.debug("Cannot consolidate null element");
      return;
    }
    
    System.out.println("Consolidating element: " + element.getUid());
  }
}
