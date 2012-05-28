package com.petpet.c3po.adaptor.fits;

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
    final Property property = this.getProperty(propKey);
    final Source source = this.cache.getSource(toolname, version);

    final MetadataRecord r = new MetadataRecord();
    r.setProperty(property);
    r.setValue(value);
    r.getSources().add(source.getId());

    if (status != null) {
      r.setStatus(status);
    }

    this.addValue(r);
  }

  public void createIdentity(String format, String mimetype) {
    final Property pf = this.getProperty("format");
    final Property pm = this.getProperty("mimetype");

    MetadataRecord fmt = new MetadataRecord();
    fmt.setProperty(pf);
    fmt.setValue(format);
    fmt.getSources().addAll(this.formatSources);

    MetadataRecord mime = new MetadataRecord();
    mime.setProperty(pm);
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
    final Property pf = this.getProperty("format.version");
    final Source s = this.cache.getSource(toolname, version);
    final MetadataRecord fmtv = new MetadataRecord();

    fmtv.setProperty(pf);
    fmtv.setValue(value);
    fmtv.getSources().add(s.getId());

    if (status != null) {
      fmtv.setStatus(status);
    }

    this.addValue(fmtv);
  }

  public void createPuid(String value, String toolname, String version) {
    final Property pp = this.getProperty("puid");
    final Source s = this.cache.getSource(toolname, version);
    final MetadataRecord puid = new MetadataRecord();

    puid.setProperty(pp);
    puid.setValue(value);
    puid.getSources().add(s.getId());

    this.addValue(puid);
  }

  private String substringPath(String str) {
    str = str.substring(str.lastIndexOf("/") + 1);
    str = str.substring(str.lastIndexOf("\\") + 1);
    return str;
  }

  private void updateStatusOf(String pName, String status) {
    Property property = this.getProperty(pName);
    for (MetadataRecord v : this.values) {
      if (v.getProperty().getId().equals(property.getId())) {
        v.setStatus(status);
      }
    }
  }

  private Property getProperty(String name) {
    final String prop = FITSHelper.getPropertyKeyByFitsName(name);
    return this.cache.getProperty(prop);
  }

}
