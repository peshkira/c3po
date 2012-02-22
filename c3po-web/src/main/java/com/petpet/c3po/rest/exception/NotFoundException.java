package com.petpet.c3po.rest.exception;

public class NotFoundException extends ApiException {

	public NotFoundException(String msg) {
		super(404, msg);
	}
}
