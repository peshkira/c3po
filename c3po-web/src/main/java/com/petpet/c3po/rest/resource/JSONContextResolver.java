package com.petpet.c3po.rest.resource;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JSONContextResolver extends org.codehaus.jackson.jaxrs.JacksonJsonProvider{
	private static ObjectMapper commonMapper = null;

	public JSONContextResolver() {
		if(commonMapper == null){
            AnnotationIntrospector jackson = new JacksonAnnotationIntrospector();
            AnnotationIntrospector jaxb = new JaxbAnnotationIntrospector();
            AnnotationIntrospector pair = new AnnotationIntrospector.Pair(jaxb, jackson);
            ObjectMapper mapper = new ObjectMapper();
            mapper.getSerializationConfig().setAnnotationIntrospector(jaxb);
            mapper.getDeserializationConfig().setAnnotationIntrospector(pair);
            mapper.getDeserializationConfig().set(Feature.AUTO_DETECT_SETTERS, true);
            mapper.configure(Feature.AUTO_DETECT_SETTERS, true);
            mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
            mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

			commonMapper = mapper;
		}
		super.setMapper(commonMapper);
	}
}
