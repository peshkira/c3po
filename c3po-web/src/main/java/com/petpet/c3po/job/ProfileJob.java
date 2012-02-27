package com.petpet.c3po.job;

import java.util.List;
import java.util.UUID;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.controller.ProfileGenerator;
import com.petpet.c3po.db.PreparedQueries;

public class ProfileJob implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileJob.class);

  private String id;

  private String collection;

  private List<String> params;

  private PreparedQueries pq;

  public ProfileJob(PreparedQueries pq, String coll, List<String> params) {
    this.setId(UUID.randomUUID().toString());
    this.collection = coll;
    this.params = params;
    this.pq = pq;
  }

  @Override
  public void run() {
    // TODO run gatherer in local folder
    // TODO run aggregator
    // TODO store output in a file named with the uuid
    LOG.info("Generating profile...");

    ProfileGenerator gen = new ProfileGenerator(collection, params, pq);
    Document profile = gen.generateProfile();
    gen.write(profile, String.format("profiles/%s.xml", id));

    LOG.info("Job {} finished", this.id);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
