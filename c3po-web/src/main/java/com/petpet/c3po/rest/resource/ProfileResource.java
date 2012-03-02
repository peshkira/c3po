package com.petpet.c3po.rest.resource;

import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.controller.ProfileGenerator;
import com.petpet.c3po.db.PreparedQueries;
import com.petpet.c3po.job.ProfileJob;
import com.sun.jersey.spi.resource.Singleton;
import com.wordnik.swagger.core.Api;
import com.wordnik.swagger.core.ApiError;
import com.wordnik.swagger.core.ApiErrors;
import com.wordnik.swagger.core.ApiOperation;
import com.wordnik.swagger.core.ApiParam;
import com.wordnik.swagger.core.ApiResponse;
import com.wordnik.swagger.core.JavaHelp;

@Path("/profile")
@Api(value = "/profile", description = "Operations about profile job submission")
@Singleton
public class ProfileResource extends JavaHelp {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileResource.class);

  private PersistenceLayer pl;

  @POST
  @Path("/create")
  @ApiOperation(value = "Submit Profile Job", notes = "")
  @ApiErrors(value = { @ApiError(code = 404, reason = "Collection not found") })
  @ApiResponse(value = "An uuid of the job")
  public Response createProfile(
      @ApiParam(value = "Collection name", required = true) @QueryParam("collection") String collection,
      @ApiParam(value = "Property names that should be expanded", required = false) @QueryParam("expanded") List<String> expanded) {

    PreparedQueries pq = new PreparedQueries(pl.getEntityManager());
    ProfileJob job = new ProfileJob(pq, collection, expanded);
    new Thread(job).start();
    

    return Response.ok().entity(job.getId()).build();
  }

  @GET
  @Path("/hello")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Prints test message", notes = "no notes")
  @ApiResponse(value = "the test message")
  public String hello() {
    return "hello world";
  }

  @PostConstruct
  public void postconstruct() {
    LOG.info("Initializing ProfileResource Rest Serivce");

    try {
      this.pl = (PersistenceLayer) new InitialContext().lookup("java:global/c3po-web/C3POPersistenceLayer");
    } catch (NamingException e) {
      LOG.error("failed to inject persistence layer: {}", e.getMessage());
    }
  }

}
