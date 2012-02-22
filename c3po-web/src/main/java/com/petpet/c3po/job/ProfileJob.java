package com.petpet.c3po.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.utils.ConfigurationException;
import com.petpet.c3po.common.Config;
import com.petpet.c3po.controller.ProfileGenerator;
import com.petpet.c3po.datamodel.DigitalCollection;
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
        DigitalCollection coll = pq.getCollectionByName(collection);
//        SimpleGatherer g = new SimpleGatherer(new FITSMetaDataAdaptor(), coll);
//        g.gather(new File("/Users/petar/Desktop/fits/235/"));

        try {
            Map<String, Object> config = new HashMap<String, Object>();
            config.put(Config.COLLECTION_CONF, coll);
            config.put(Config.EXPANDED_PROPS_CONF, params);
            ProfileGenerator gen = new ProfileGenerator(pq);
            gen.configure(config);
            Document profile = gen.generateProfile();
            gen.write(profile, String.format("profiles/%s.xml", id));
        } catch (ConfigurationException e) {
            LOG.error("An error occurred while generating the profile: {}", e.getMessage());
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
