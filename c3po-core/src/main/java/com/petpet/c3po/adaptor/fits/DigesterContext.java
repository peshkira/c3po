package com.petpet.c3po.adaptor.fits;

import java.util.ArrayList;
import java.util.List;

import com.petpet.c3po.api.adaptor.PreProcessingRule;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.api.dao.ReadOnlyCache;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;

public class DigesterContext {

  private ReadOnlyCache cache;

  private Element element;

  private List<MetadataRecord> values;

  private List<String> formatSources;

  private List<PreProcessingRule> rules;

  public DigesterContext(ReadOnlyCache cache, List<PreProcessingRule> rules) {
    this.cache = cache;
    this.values = new ArrayList<MetadataRecord>();
    this.formatSources = new ArrayList<String>();
    this.rules = rules;
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

    boolean shouldContinue = true;

    for (PreProcessingRule r : rules) {
      if (r.shouldSkip(propKey, value, status, toolname, version)) {
        shouldContinue = false;
        break;
      }
    }

    if (shouldContinue) {
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
  }

  public void createIdentity(String format, String mimetype) {
    final Property pf = this.getProperty("format");
    final Property pm = this.getProperty("mimetype");
    
    this.createIdentityForProperty(pf, format);
    this.createIdentityForProperty(pm, mimetype);
    
    this.formatSources.clear();
  }
  
  private void createIdentityForProperty(Property property, String value) {
    boolean shouldContinue = true;
    
    for (PreProcessingRule r : rules) {
      if (r.shouldSkip(property.getId(), value, null, null, null)) {
        shouldContinue = false;
        break;
      }
    }
    
    if (shouldContinue) {
      MetadataRecord rec = new MetadataRecord();
      rec.setProperty(property);
      rec.setValue(value);
      rec.getSources().addAll(this.formatSources);
      this.addValue(rec);
    }
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
    final Property pf = this.getProperty("format_version");
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
    if (str != null) {
      str = str.substring(str.lastIndexOf("/") + 1);
      str = str.substring(str.lastIndexOf("\\") + 1);
    }
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
