package com.petpet.collpro.datamodel;

import java.util.Date;
import java.util.Set;

public class RepresentativeCollection {

  private Date createdAt;
  
  private DigitalCollection collection;
  
  private Set<Element> elements;
  
  public RepresentativeCollection() {
    this.createdAt = new Date();
  }
  
  public RepresentativeCollection(DigitalCollection coll, Set<Element> elmnts) {
    this.collection = coll;
    this.elements = elmnts;
    this.createdAt = new Date();
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public DigitalCollection getCollection() {
    return collection;
  }

  public Set<Element> getElements() {
    return elements;
  }
  
  public int getSize() {
    return this.elements.size();
  }

}
