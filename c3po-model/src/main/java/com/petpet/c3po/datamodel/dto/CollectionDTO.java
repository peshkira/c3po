package com.petpet.c3po.datamodel.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionDTO {
  
  @XmlAttribute
  private String name;

  @XmlAttribute(name = "elements")
  private int element_count;

  @XmlAttribute(name = "properties")
  private int property_count;

  @XmlElementWrapper(name = "properties")
  @XmlElement(name = "property")
  private List<PropertyDTO> properties;

  public int getElement_count() {
    return element_count;
  }

  public void setElement_count(int element_count) {
    this.element_count = element_count;
  }

  public int getProperty_count() {
    return property_count;
  }

  public void setProperty_count(int property_count) {
    this.property_count = property_count;
  }

  public List<PropertyDTO> getProperties() {
    return properties;
  }

  public void setProperties(List<PropertyDTO> properties) {
    this.properties = properties;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
