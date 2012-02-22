package com.petpet.c3po.rest.resource;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.resource.Singleton;
import com.wordnik.swagger.core.Api;
import com.wordnik.swagger.core.ApiError;
import com.wordnik.swagger.core.ApiErrors;
import com.wordnik.swagger.core.ApiOperation;
import com.wordnik.swagger.core.ApiParam;
import com.wordnik.swagger.core.ApiResponse;
import com.wordnik.swagger.core.JavaHelp;

@Path("/swagger/profile")
@Api(value = "", description = "Operations about profile job submission")
@Singleton
public class ProfileResource extends JavaHelp {

  @POST
  @Path("/create")
  @ApiOperation(value = "Submit Profile Job", notes = "")
  @ApiErrors(value = { @ApiError(code = 404, reason = "Collection not found") })
  @ApiResponse(value = "An uuid of the job")
  public Response createProfile(
      @ApiParam(value = "Collection name", required = true) @QueryParam("collection") String collection,
      @ApiParam(value = "Property names that should be expanded", required = false) @QueryParam("expanded") List<String> expanded) {

    return Response.ok().entity(UUID.randomUUID().toString()).build();
  }

}
