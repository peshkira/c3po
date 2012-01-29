package com.petpet.c3po.beans;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dom4j.Document;

@Stateless(name = "pgService")
public class ProfileGeneratorService {

    @PersistenceContext(unitName = "C3POPersistenceUnit")
    private EntityManager em;

    // private ProfileGenerator generator;

    @PostConstruct
    public void init() {
        // this.generator = new ProfileGenerator(new PreparedQueries(em));

    }

    public Document getProfile(String name, String... props) {
        // this.generator.addParameter(Config.COLLECTION_CONF,
        // name).addParameter(Config.EXPANDED_PROPS_CONF, props);
        // return this.generator.generateProfile();
        return null;
    }

}
