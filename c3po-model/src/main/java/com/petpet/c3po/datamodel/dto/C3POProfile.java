package com.petpet.c3po.datamodel.dto;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "collection-profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class C3POProfile {

  @XmlAttribute(name = "date")
  private Date createdAt;
  
  @XmlElement
  private CollectionDTO collection;
  
  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public CollectionDTO getCollection() {
    return collection;
  }

  public void setCollection(CollectionDTO collection) {
    this.collection = collection;
  }

}
