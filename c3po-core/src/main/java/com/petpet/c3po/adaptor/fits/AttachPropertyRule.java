package com.petpet.c3po.adaptor.fits;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester3.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Value;

public class AttachPropertyRule extends Rule {

  private static final Logger LOG = LoggerFactory.getLogger(AttachPropertyRule.class);

  public static final String GENERIC = "42";

  private String prop;

  public AttachPropertyRule(String prop) {
    this.prop = prop;

    if (this.prop == null || this.prop.equals("")) {
      this.prop = GENERIC;
    }
  }

  @Override
  public void begin(String namespace, String name, Attributes attributes) throws Exception {
    super.begin(namespace, name, attributes);

    Property property = null;
    if (this.prop.equals(GENERIC)) {
      property = this.getProperty(name);
    } else if (this.prop.equals("format") || this.prop.equals("mimetype") || this.prop.equals("format.version") || this.prop.equals("puid")) {
      property = this.getProperty(this.prop);
    }

    LOG.debug("Attaching Property {} and type {}", property.getName(), property.getType().name());
    final Value v = (Value) this.getDigester().peek();
    v.setProperty(property);

  }

  @Override
  public void end(String namespace, String name) throws Exception {
    if (this.prop.equals(GENERIC)) {
      super.end(namespace, name);

    } else {
      LOG.debug("Property is: {}", this.prop);
      Value v1 = null;
      Value v2 = null;
      Value v3 = null;
     

      if (this.getDigester().peek() instanceof Value<?>) {
        v1 = (Value) this.getDigester().pop();
        LOG.debug("Popped value: {}", v1.getValue());
      }

      if (this.getDigester().peek() instanceof Value<?>) {
        v2 = (Value) this.getDigester().pop();
        LOG.debug("temporary popping value: {}", v2.getValue());
      }
      
      if (this.getDigester().peek() instanceof Value<?>) {
        v3 = (Value) this.getDigester().pop();
        LOG.debug("temporary popping value: {}", v3.getValue());
      }

      Element e = (Element) this.getDigester().peek();
      e.addValue(v1);
      LOG.debug("Added value to element {}", e);

      LOG.debug("Pushing back values");

      if (v3 != null) {
        this.getDigester().push(v3);
      }
      
      if (v2 != null) {
        this.getDigester().push(v2);
      }
      this.getDigester().push(v1);
    }

  }

  private Property getProperty(String name) {
    return FITSHelper.getPropertyByFitsName(name);
  }

}
