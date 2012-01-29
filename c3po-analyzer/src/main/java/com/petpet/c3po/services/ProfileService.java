package com.petpet.c3po.services;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.beans.ProfileGeneratorService;

@Path("/profile")
@Stateless
public class ProfileService {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);

    @EJB(beanName = "pgService")
    ProfileGeneratorService pgService;

    @PostConstruct
    public void init() {
        LOG.info("POST CONSTRUCT CALLED");
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_XML)
    public String hello(@QueryParam("collection") String coll, @QueryParam("expanded") List<String> expanded) {
        
        LOG.info("Collection: {}", coll);
        for (String s: expanded) {
            LOG.info("Prop: {}", s);
        }
        
        Document profile = pgService.getProfile("Test", new String[0]);
        if (profile != null) {
            return profile.asXML();
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<error>Internal Generation Error</error>";
    }

}
