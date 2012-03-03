package com.petpet.c3po.job;

import java.util.List;
import java.util.UUID;

import javax.persistence.NoResultException;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.controller.ProfileGenerator;
import com.petpet.c3po.db.PreparedQueries;

public class ProfileJob implements Runnable {

  public static final String ERROR_ID = "ERROR";

  private static final Logger LOG = LoggerFactory.getLogger(ProfileJob.class);

  private String id;

  private String collection;

  private List<String> params;

  private PreparedQueries pq;

  private boolean running;

  public ProfileJob(PreparedQueries pq, String coll, List<String> params) {
    this.running = false;
    this.pq = pq;
    this.params = params;
    this.collection = coll;
    this.init();
  }

  private void init() {
    try {
      this.pq.getCollectionByName(this.collection);
      this.id = UUID.randomUUID().toString();
    } catch (NoResultException e) {
      this.id = ERROR_ID;
    }
  }

  @Override
  public void run() {
    LOG.info("Generating profile...");
    this.running = true;

    ProfileGenerator gen = new ProfileGenerator(collection, params, pq);
    Document profile = gen.generateProfile();

    if (profile != null) {
      gen.write(profile, String.format("profiles/%s.xml", id));
    }

    this.running = false;
    LOG.info("Job {} finished", this.id);
  }

  public String getId() {
    return id;
  }

  public boolean isRunning() {
    return running;
  }

}
