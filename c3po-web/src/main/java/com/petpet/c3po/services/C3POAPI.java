package com.petpet.c3po.services;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.petpet.c3po.rest.resource.ProfileResource;


public class C3POAPI extends Application {
  
  @Override
  public Set<Class<?>> getClasses() {
    final Set<Class<?>> classes = new HashSet<Class<?>>();
    
    classes.add(ProfileResource.class);
    
    return classes;
  }

}
