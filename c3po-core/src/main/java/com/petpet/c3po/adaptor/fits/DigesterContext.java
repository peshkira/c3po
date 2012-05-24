package com.petpet.c3po.adaptor.fits;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.MetadataRecord.Status;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Source;

public class DigesterContext {

  private Cache cache;

  private Element element;

  private List<MetadataRecord> values;

  // private MetadataRecord fmt;
  //
  // private MetadataRecord mime;

  private List<String> formatSources;

  public DigesterContext(Cache cache) {
    this.cache = cache;
    this.values = new ArrayList<MetadataRecord>();
    this.formatSources = new ArrayList<String>();
  }

  public List<MetadataRecord> getValues() {
    return values;
  }

  public Element getElement() {
    return element;
  }

  public void addValue(MetadataRecord value) {
    this.getValues().add(value);
  }

  public void createElement(String name, String uid) {
    this.element = new Element(uid, this.substringPath(name));
  }

  public void createValue(String value, String status, String toolname, String version, String pattern) {
    final String propKey = this.substringPath(pattern);
    final Property property = this.cache.getProperty(propKey);
    final Source source = this.cache.getSource(toolname, version);

    final MetadataRecord r = new MetadataRecord();
    r.setPRef(property.getId());
    r.setValue(value);
    r.getSources().add(source.getId());

    if (status != null) {
      r.setStatus(status);
    }

    this.addValue(r);
  }

  public void createIdentity(String format, String mimetype) {
    final Property pf = this.cache.getProperty("format");
    final Property pm = this.cache.getProperty("mimetype");
    // final Source s = this.cache.getSource(toolname, version);

    MetadataRecord fmt = new MetadataRecord();
    fmt.setPRef(pf.getId());
    fmt.setValue(format);
    fmt.getSources().addAll(this.formatSources);

    MetadataRecord mime = new MetadataRecord();
    mime.setPRef(pm.getId());
    mime.setValue(mimetype);
    mime.getSources().addAll(this.formatSources);

    this.addValue(fmt);
    this.addValue(mime);
    this.formatSources.clear();
  }

  public void addIdentityTool(String toolname, String version) {
    final Source s = this.cache.getSource(toolname, version);
    this.formatSources.add(s.getId());
  }

  // TODO test this
  public void setIdentityStatus(String status) {
    if (status != null) {
      if (status.equals(MetadataRecord.Status.SINGLE_RESULT.name())) {
        this.updateStatusOf("puid", Status.CONFLICT.name());
      }
      
      this.updateStatusOf("format", status);
      this.updateStatusOf("mimetype", status);

    }
  }

  public void createFormatVersion(String value, String status, String toolname, String version) {
    final Property pf = this.cache.getProperty("format-version");
    final Source s = this.cache.getSource(toolname, version);
    final MetadataRecord fmtv = new MetadataRecord();

    fmtv.setPRef(pf.getId());
    fmtv.setValue(value);
    fmtv.getSources().add(s.getId());

    if (status != null) {
      fmtv.setStatus(status);
    }

    this.addValue(fmtv);
  }

  public void createPuid(String value, String toolname, String version) {
    final Property pp = this.cache.getProperty("puid");
    final Source s = this.cache.getSource(toolname, version);
    final MetadataRecord puid = new MetadataRecord();

    puid.setPRef(pp.getId());
    puid.setValue(value);
    puid.getSources().add(s.getId());

    this.addValue(puid);
  }

  private String substringPath(String str) {
    return str.substring(str.lastIndexOf(File.separator) + 1);
  }

  // TODO get the correct reference...
  private void updateStatusOf(String pName, String status) {
    Property property = this.cache.getProperty(pName);
    for (MetadataRecord v : this.values) {
      if (v.getPRef().equals(property.getId())) {
        v.setStatus(status);
      }
    }
  }

}
