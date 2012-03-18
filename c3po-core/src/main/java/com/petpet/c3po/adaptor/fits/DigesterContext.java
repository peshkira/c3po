package com.petpet.c3po.adaptor.fits;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.StringValue;
import com.petpet.c3po.datamodel.Value;
import com.petpet.c3po.datamodel.ValueSource;
import com.petpet.c3po.datamodel.ValueStatus;
import com.petpet.c3po.utils.DBHelper;
import com.petpet.c3po.utils.Helper;

public class DigesterContext {

  private static final Logger LOG = LoggerFactory.getLogger(DigesterContext.class);

  private Element element;

  private List<Value<?>> values = new ArrayList<Value<?>>();

  public List<Value<?>> getValues() {
    return values;
  }

  public Element getElement() {
    return element;
  }

  public void addValue(Value<?> value) {
    this.getValues().add(value);
  }

  public void setIdentityStatus(String status) {

    if (status != null) {
      if (status.equals(ValueStatus.CONFLICT.name())) {
        this.updateStatusOf("format");
      } else {
        this.updateStatusOf("puid");
      }
    }
  }

  public void createElement(String name, String uid) {
    this.element = new Element(name, uid);
  }

  public void createValue(String value, String status, String toolname, String version, String pattern) {
    final Property property = this.getProperty(pattern.substring(pattern.lastIndexOf('/') + 1));
    final ValueSource source = this.getValueSource(toolname, version);

    Value<?> v = Helper.getTypedValue(property.getType(), value);

    if (status != null)
      v.setStatus(status);

    v.setSource(source);
    v.setProperty(property);

    this.addValue(v);
  }

  public void createIdentity(String format, String mimetype, String toolname, String version) {
    final Property pFormat = this.getProperty("format");
    final Property pMime = this.getProperty("mimetype");
    final ValueSource source = this.getValueSource(toolname, version);

    final StringValue vFormat = new StringValue(format);
    final StringValue vMime = new StringValue(mimetype);

    vFormat.setSource(source);
    vFormat.setProperty(pFormat);
    vMime.setSource(source);
    vMime.setProperty(pMime);

    this.addValue(vFormat);
    this.addValue(vMime);
  }

  public void createFormatVersion(String value, String status, String toolname, String version) {
    final Property p = this.getProperty("format.version");
    final StringValue v = new StringValue(value);
    final ValueSource s = this.getValueSource(toolname, version);

    if (status != null) {
      v.setStatus(status);
    }

    v.setSource(s);
    v.setProperty(p);
    this.addValue(v);
  }

  public void createPuid(String value, String toolname, String version) {
    final Property p = this.getProperty("puid");
    final ValueSource s = this.getValueSource(toolname, version);
    final StringValue v = new StringValue(value);

    v.setSource(s);
    v.setProperty(p);
    this.addValue(v);
  }

  private Property getProperty(String name) {
    // TODO move this to DBHelper...
    return FITSHelper.getPropertyByFitsName(name);
  }

  private ValueSource getValueSource(String toolname, String version) {
    ValueSource s = DBHelper.getValueSource(toolname, version);
    if (s == null) {
      s = new ValueSource(toolname, version);
    }

    return s;
  }

  private void updateStatusOf(String pName) {
    for (Value<?> v : this.values) {
      if (v.getProperty().getName().equals(pName)) {
        v.setStatus(ValueStatus.CONFLICT.name());
      }
    }
  }

}
