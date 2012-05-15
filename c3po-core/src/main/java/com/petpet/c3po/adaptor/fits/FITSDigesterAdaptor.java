package com.petpet.c3po.adaptor.fits;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RegexRules;
import org.apache.commons.digester3.SimpleRegexMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;

public class FITSDigesterAdaptor implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(FITSDigesterAdaptor.class);

  private InputStream stream;

  private Digester digester;

  public FITSDigesterAdaptor() {
    this.digester = new Digester(); // not thread safe
    this.digester.setRules(new RegexRules(new SimpleRegexMatcher()));
    this.createRules();
  }

  private void createRules() {
    this.createElementRules();
    this.createIdentityRules();
    this.createFileInfoRules();
    this.createFileStatusRules();
    this.createMetaDataRules();
  }

  private void createElementRules() {
    this.digester.addCallMethod("fits", "createElement", 2);
    this.digester.addCallParam("fits/fileinfo/filename", 0);
    this.digester.addCallParam("fits/fileinfo/filepath", 1);
  }

  private void createIdentityRules() {
    this.createIdentityStatusRules();

    this.createFormatRule("fits/identification/identity");
    this.createFormatVersionRule("fits/identification/identity/version");
    this.createPuidRule("fits/identification/identity/externalIdentifier");

  }

  private void createFileInfoRules() {
    this.createValueRule("fits/fileinfo/size");
    this.createValueRule("fits/fileinfo/md5checksum");
    this.createValueRule("fits/fileinfo/lastmodified");
    this.createValueRule("fits/fileinfo/fslastmodified");
    this.createValueRule("fits/fileinfo/created");
    this.createValueRule("fits/fileinfo/creatingApplicationName");
    this.createValueRule("fits/fileinfo/creatingApplicationVersion");
    this.createValueRule("fits/fileinfo/inhibitorType");
    this.createValueRule("fits/fileinfo/inhibitorTarget");
    this.createValueRule("fits/fileinfo/rightsBasis");
    this.createValueRule("fits/fileinfo/copyrightBasis");
    this.createValueRule("fits/fileinfo/copyrightNote");
    this.createValueRule("fits/fileinfo/creatingos");
  }

  private void createFileStatusRules() {
    this.createValueRule("fits/filestatus/well-formed");
    this.createValueRule("fits/filestatus/valid");
    this.createValueRule("fits/filestatus/message");
  }

  private void createMetaDataRules() {
    this.createValueRule("fits/metadata/image/*");
    this.createValueRule("fits/metadata/text/*");
    this.createValueRule("fits/metadata/document/*");
    this.createValueRule("fits/metadata/audio/*");
    this.createValueRule("fits/metadata/video/*");
  }

  private void createIdentityStatusRules() {
    this.digester.addCallMethod("fits/identification", "setIdentityStatus", 1);
    this.digester.addCallParam("fits/identification", 0, "status");
  }

  private void createFormatRule(String pattern) {
    this.digester.addCallMethod(pattern, "createIdentity", 4);
    this.digester.addCallParam(pattern, 0, "format");
    this.digester.addCallParam(pattern, 1, "mimetype");
    this.digester.addCallParam(pattern + "/tool", 2, "toolname");
    this.digester.addCallParam(pattern + "/tool", 3, "toolversion");

  }

  private void createFormatVersionRule(String pattern) {
    this.digester.addCallMethod(pattern, "createFormatVersion", 4);
    this.digester.addCallParam(pattern, 0);
    this.digester.addCallParam(pattern, 1, "status");
    this.digester.addCallParam(pattern, 2, "toolname");
    this.digester.addCallParam(pattern, 3, "toolversion");
  }

  private void createPuidRule(String pattern) {
    this.digester.addCallMethod(pattern, "createPuid", 3);
    this.digester.addCallParam(pattern, 0);
    this.digester.addCallParam(pattern, 1, "toolname");
    this.digester.addCallParam(pattern, 2, "toolversion");

  }

  private void createValueRule(String pattern) {
    this.digester.addCallMethod(pattern, "createValue", 5);
    this.digester.addCallParam(pattern, 0);
    this.digester.addCallParam(pattern, 1, "status");
    this.digester.addCallParam(pattern, 2, "toolname");
    this.digester.addCallParam(pattern, 3, "toolversion");
    this.digester.addCallParamPath(pattern, 4);
  }

  public Element getElement() {
    if (this.stream == null) {
      LOG.warn("The input stream is not set, skipping.");
      return null;
    }

    try {
      this.digester.push(new DigesterContext());
      DigesterContext context = (DigesterContext) this.digester.parse(this.stream);
      Element element = this.postProcess(context);
      return element;

    } catch (IOException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } finally {
      try {
        this.stream.close();
        this.stream = null;
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }

    return null;
  }

  private Element postProcess(DigesterContext context) {
    final Element element = context.getElement();
    final List<MetadataRecord> values = context.getValues();

    if (element == null) {
      LOG.warn("No element could be extracted");
    } else {
      element.setMetadata(values);
    }

    return element;
  }

  @Override
  public void run() {
    final Element element = this.getElement();
    if (element != null) {
      // this.controller.processElement(element);
      // TODO
    } else {
      LOG.warn("No element could be extracted");
    }

  }

  public InputStream getStream() {
    return stream;
  }

  public void setStream(InputStream stream) {
    this.stream = stream;
  }

}
