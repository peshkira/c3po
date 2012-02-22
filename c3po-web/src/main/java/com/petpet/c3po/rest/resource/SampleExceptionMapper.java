package com.petpet.c3po.rest.resource;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.petpet.c3po.rest.exception.ApiException;
import com.petpet.c3po.rest.exception.BadRequestException;
import com.petpet.c3po.rest.exception.NotFoundException;
import com.petpet.c3po.rest.model.ApiResponse;


@Provider
public class SampleExceptionMapper implements ExceptionMapper<ApiException> {
	public Response toResponse(ApiException exception) {
		if (exception instanceof NotFoundException) {
			return Response
					.status(Status.NOT_FOUND)
					.entity(new ApiResponse(ApiResponse.ERROR, exception
							.getMessage())).build();
		} else if (exception instanceof BadRequestException) {
			return Response
					.status(Status.BAD_REQUEST)
					.entity(new ApiResponse(ApiResponse.ERROR, exception
							.getMessage())).build();
		} else if (exception instanceof ApiException) {
			return Response
					.status(Status.BAD_REQUEST)
					.entity(new ApiResponse(ApiResponse.ERROR, exception
							.getMessage())).build();
		} else {
			return Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(new ApiResponse(ApiResponse.ERROR,
							"a system error occured")).build();
		}
	}
}
