package com.petpet.c3po.adaptor.fits;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;

//TODO create property and source cache and add the new properties and sources
//accordingly...
public class DigesterContext {

  private Element element;

  private List<MetadataRecord> values = new ArrayList<MetadataRecord>();

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
    this.element = new Element(this.substringPath(name), uid);
  }

  public void createValue(String value, String status, String toolname, String version, String pattern) {
    final MetadataRecord r = new MetadataRecord();
    r.setKey(this.substringPath(pattern));
    r.setValue(value);
    r.setStatus(status);
    this.addValue(r);
  }

  public void createIdentity(String format, String mimetype, String toolname, String version) {
    MetadataRecord fmt = new MetadataRecord();
    fmt.setKey("format");
    fmt.setValue(format);

    MetadataRecord mime = new MetadataRecord();
    mime.setKey("mimetype");
    mime.setValue(mimetype);

    this.addValue(fmt);
    this.addValue(mime);
  }
  
  public void setIdentityStatus(String status) {
    if (status != null) {
      if (status.equals(MetadataRecord.Status.CONFLICT.name()) || status.equals(MetadataRecord.Status.PARTIAL.name())) {
        this.updateStatusOf("format");
      } else {
        this.updateStatusOf("puid");
      }
    }
  }

  public void createFormatVersion(String value, String status, String toolname, String version) {
    MetadataRecord fmtv = new MetadataRecord();
    fmtv.setKey("format-version");
    fmtv.setValue(value);
    this.addValue(fmtv);
  }

  public void createPuid(String value, String toolname, String version) {
    MetadataRecord puid = new MetadataRecord();
    puid.setKey("puid");
    puid.setValue(value);

    this.addValue(puid);
  }
  
  private String substringPath(String str) {
    return str.substring(str.lastIndexOf(File.separator) + 1);
  }
  
  //TODO get the correct reference...
  private void updateStatusOf(String pName) {
//    for (MetadataRecord v : this.values) {
//      if (v.getKey().equals(pName)) {
//        v.setStatus(ValueStatus.CONFLICT.name());
//      }
//    }
  }

}
