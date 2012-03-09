package com.petpet.c3po.datamodel.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class PropertyDTO {

  @XmlAttribute
  private String id;
  
  @XmlAttribute
  private String name;
  
  @XmlAttribute
  private String type;
  
  @XmlAttribute
  private String count;
  
  @XmlAttribute
  private String mode;
  
  @XmlAttribute
  private String avg;
  
  @XmlAttribute
  private String min;
  
  @XmlAttribute
  private String max;
  
  @XmlAttribute
  private String expanded;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCount() {
    return count;
  }

  public void setCount(String count) {
    this.count = count;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getAvg() {
    return avg;
  }

  public void setAvg(String avg) {
    this.avg = avg;
  }

  public String getMin() {
    return min;
  }

  public void setMin(String min) {
    this.min = min;
  }

  public String getMax() {
    return max;
  }

  public void setMax(String max) {
    this.max = max;
  }

  public String getExpanded() {
    return expanded;
  }

  public void setExpanded(String expanded) {
    this.expanded = expanded;
  }
}
