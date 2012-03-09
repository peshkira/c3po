package com.petpet.c3po.rest.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.datamodel.dto.C3POProfile;
import com.petpet.c3po.datamodel.dto.CollectionDTO;
import com.petpet.c3po.datamodel.dto.PropertyDTO;
import com.petpet.c3po.job.ProfileJob;
import com.petpet.c3po.job.ProfileJobController;
import com.petpet.c3po.job.ProfileJobController.JobStatus;
import com.petpet.c3po.rest.exception.NotFoundException;
import com.sun.jersey.spi.resource.Singleton;
import com.wordnik.swagger.core.Api;
import com.wordnik.swagger.core.ApiError;
import com.wordnik.swagger.core.ApiErrors;
import com.wordnik.swagger.core.ApiOperation;
import com.wordnik.swagger.core.ApiParam;
import com.wordnik.swagger.core.ApiResponse;
import com.wordnik.swagger.core.JavaHelp;

@Path("/profile.json")
@Api(value = "/profile", description = "The Profile REST service")
@Singleton
@Produces(MediaType.APPLICATION_XML)
public class ProfileResource extends JavaHelp {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileResource.class);

  private ProfileJobController controller;

  @POST
  @Path("/create")
  @ApiOperation(value = "Submits a profile creation job", notes = "This operation only creates a profile dump and does not rescan for new content")
  @ApiErrors(value = { @ApiError(code = 404, reason = "Collection not found") })
  @ApiResponse(value = "An uuid of the job")
  public Response createProfile(
      @ApiParam(value = "Collection name", required = true) @QueryParam("collection") String collection) { //@ApiParam(value = "Property names that should be expanded", required = false)@QueryParam("expanded") List<String> expanded

    String uuid = this.controller.submit(collection, new ArrayList<String>());
    if (uuid.equals(ProfileJob.ERROR_ID)) {
      return new NotFoundException("Collection not found").getResponse();
    }

    return Response.ok().entity(uuid).build();
  }

  @GET
  @Path("/{uuid}")
  @ApiOperation(value = "Retrieves the previously created profile", notes = "The uuid has to be the one returned by the job creation")
  @ApiErrors(value = { @ApiError(code = 404, reason = "Job not yet ready or already retrieved") })
  public Response getProfile(@ApiParam(value = "The uuid of the job", required = true) @PathParam("uuid") String uuid) {

    File file = new File(String.format("profiles/%s.xml", uuid));

    if (!file.exists()) {
      return Response.status(Status.NOT_FOUND).build();
    }

    Response response = Response.ok().entity(file).build();
    this.controller.remove(uuid);
    return response; 
  }

  @GET
  @Path("/status/{uuid}")
  @ApiOperation(value = "Retrieves the status of the profile job", notes = "")
  @ApiResponse(value = "One of READY, RUNNING or 404")
  public Response status(@PathParam("uuid") String uuid) {
    LOG.info("received status call: {}", uuid);
    JobStatus status = this.controller.status(uuid);
    Response r = Response.serverError().build();
    switch (status) {
      case READY:
        r = Response.ok().entity("READY").build();
        break;
      case RUNNING:
        r = Response.ok().entity("RUNNING").build();
        break;
      case NOT_FOUND:
        r = new NotFoundException("No job found, id: " + uuid).getResponse();
        break;
    }
    
    return r;

  }
  
  @GET
  @Path("test")
  public Response test() {
    C3POProfile profile = new C3POProfile();
    profile.setCreatedAt(new Date());
    
    CollectionDTO coll = new CollectionDTO();
    coll.setName("Test");
    coll.setElement_count(34);
    coll.setProperty_count(1);
    
    PropertyDTO p1 = new PropertyDTO();
    p1.setCount("23");
    p1.setAvg("100");
    p1.setExpanded("false");
    p1.setMax("123");
    p1.setMin("1");
    p1.setMode("64");
    p1.setName("Some Name");
    p1.setId("some.id");
    
    PropertyDTO p2 = new PropertyDTO();
    p2.setName("other.prop");
    p2.setExpanded("false");
    
    coll.setProperties(Arrays.asList(p1, p2));
    profile.setCollection(coll);
    
    return Response.ok().entity(profile).build();
        
  }

  @PostConstruct
  public void postconstruct() {
    LOG.info("Initializing ProfileResource Rest Serivce");

    try {
      InitialContext context = new InitialContext();
      this.controller = (ProfileJobController) context.lookup("java:global/c3po-web/ProfileJobController");
    } catch (NamingException e) {
      LOG.error("failed to inject persistence layer: {}", e.getMessage());
    }
  }
  
  

}
