package com.petpet.c3po.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.db.PreparedQueries;
import com.petpet.c3po.job.ProfileJob;

@Path("/profile")
public class ProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);

    private Map<String, ProfileJob> jobs;
    
    private PreparedQueries pq;
    
    @PersistenceContext(unitName = "C3POPersistenceUnit")
    private EntityManager em;
    
    public void init() {
        if (this.jobs == null) {
            this.jobs = new HashMap<String, ProfileJob>();
            this.pq = new PreparedQueries(getEm());
        }
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_XML)
    public String create(@QueryParam("collection") String coll, @QueryParam("expanded") List<String> expanded) {
        this.init();
        ProfileJob job = new ProfileJob(pq, coll, expanded);
        this.jobs.put(job.getId(), job);

        new Thread(job).start();
        LOG.info("Starting Job {} on collection {}", job.getId(), coll);

        return job.getId();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String get(@QueryParam("job") String job) {
        //TODO retrieve the profile job and remove from map...
        return "";
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = em;
    }

}
