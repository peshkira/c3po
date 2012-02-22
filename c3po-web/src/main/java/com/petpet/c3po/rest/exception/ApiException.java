package com.petpet.c3po.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ApiException extends WebApplicationException {
	private int code;

	public ApiException(int code, String message) {
		super(Response.status(code).entity(message).type(MediaType.TEXT_PLAIN)
				.build());
		this.code = code;
	}

	public ApiException(int code, Throwable e) {
		super(Response.status(code)
				.entity(e.getClass().getSimpleName() + ": " + e.getMessage())
				.type(MediaType.TEXT_PLAIN).build());

		this.code = code;
	}
}
