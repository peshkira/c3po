package com.petpet.c3po.job;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileJob implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileJob.class);

    private String id;

    private String collection;

    private List<String> params;

    public ProfileJob(String coll, List<String> params) {
        this.setId(UUID.randomUUID().toString());
        this.collection = coll;
        this.params = params;
    }

    @Override
    public void run() {
        // TODO run gatherer in local folder
        // TODO run aggregator
        // TODO store output in a file named with the uuid
        int i = 10;
        while (i > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            i--;

            LOG.info("Generating profile...");
        }

        LOG.info("Job {} finished", this.id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
