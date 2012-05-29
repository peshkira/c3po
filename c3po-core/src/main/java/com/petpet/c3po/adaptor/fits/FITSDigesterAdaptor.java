package com.petpet.c3po.adaptor.fits;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RegexRules;
import org.apache.commons.digester3.SimpleRegexMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.DataHelper;

public class FITSDigesterAdaptor implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(FITSDigesterAdaptor.class);

  private InputStream stream;

  private String file;

  private Digester digester;

  private Controller controller;

  public FITSDigesterAdaptor(Controller ctrl) {
    this.controller = ctrl;
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
    this.digester.addCallMethod(pattern, "createIdentity", 2);
    this.digester.addCallParam(pattern, 0, "format");
    this.digester.addCallParam(pattern, 1, "mimetype");

    this.digester.addCallMethod(pattern + "/tool", "addIdentityTool", 2);
    this.digester.addCallParam(pattern + "/tool", 0, "toolname");
    this.digester.addCallParam(pattern + "/tool", 1, "toolversion");

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
    try {
      this.stream = new FileInputStream(this.file);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    if (this.stream == null) {
      LOG.warn("The input stream is not set, skipping.");
      return null;
    }

    try {
      this.digester.push(new DigesterContext(this.controller.getCache()));
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
        // LOG.info("stream of element closed");
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
      element.setCollection(this.controller.getCollection());
    }

    return element;
  }

  @Override
  public void run() {
    String next = this.controller.getNext();

    Property property = this.controller.getCache().getProperty("created");

    while (next != null) {
      this.file = next;

      final Element element = this.getElement();

      if (element != null) {
        // this.controller.processElement(element);

//        String date = this.extractDate(element.getName());
//        if (date != null) {
//          MetadataRecord created = new MetadataRecord(property, "20050808122324");
//          element.getMetadata().add(created);
//        }

        // LOG.info("{}", file);
        this.controller.getPersistence().insert("elements", DataHelper.getDocument(element));

      } else {
        LOG.warn("No element could be extracted");
      }

      next = this.controller.getNext();
    }
  }

  /*
   * experimental only for the SB archive
   */
  public String extractDate(String name) {
    String[] split = name.split("-");
    if (split.length >= 2) {
      String date = split[2];

      try {
        Long.valueOf(date);
      } catch (NumberFormatException nfe) {
        // if the value is not a number then it is something else and not a
        // year, skip the inference.
        return null;
      }
      // LOG.info("new value added {}", e.getName());
      return date;
    }

    return null;
  }

}
