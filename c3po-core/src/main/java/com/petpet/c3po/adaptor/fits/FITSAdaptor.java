package com.petpet.c3po.adaptor.fits;

import java.io.IOException;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RegexRules;
import org.apache.commons.digester3.SimpleRegexMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataStream;

/**
 * An adaptor for FITS <url>https://github.com/harvard-lts/fits</url> meta data.
 * It makes use of the Apache Commons Digester to parse the files.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class FITSAdaptor extends AbstractAdaptor {

  /**
   * A default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FITSAdaptor.class);

  /**
   * The apache digester to process the fits xml meta data.
   */
  private Digester digester;

  /**
   * A default constructor that initialises the digester and sets up the parsing
   * rules.
   */
  public FITSAdaptor() {
    this.digester = new Digester(); // not thread safe
    this.digester.setRules(new RegexRules(new SimpleRegexMatcher()));
    this.createParsingRules();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void configure() {

  }

  /**
   * Parses the meta data and retrieves it. This method makes use of a
   * {@link DigesterContext} object that is pushed upon the digester stack. This
   * helper object has some methods for handling some special cases.
   */
  @Override
  public Element parseElement(MetadataStream stream) {
    Element element = null;
    try {

      DigesterContext context = new DigesterContext(this.getCache(), this.getPreProcessingRules());
      this.digester.push(context);
      context = (DigesterContext) this.digester.parse(stream.getData());
      element = context.getElement();
      List<MetadataRecord> values = context.getValues();

      if (element != null) {
        element.setMetadata(values);
      }

    } catch (IOException e) {
      LOG.warn("An exception occurred while processing {}: {}", stream.getFileName(), e.getMessage());
    } catch (SAXException e) {
      LOG.warn("An exception occurred while parsing {}: {}", stream.getFileName(), e.getMessage());
    } catch (Exception e) {
      LOG.warn("An exception occurred while parsing {}: {}", stream.getFileName(), e.getMessage());
    }

    return element;
  }

  /**
   * Returns the prefix of this adaptor ('fits').
   */
  @Override
  public String getAdaptorPrefix() {
    return "fits";
  }

  /**
   * Creates SAX based rules.
   */
  private void createParsingRules() {
    this.createElementRules();
    this.createIdentityRules();
    this.createFileInfoRules();
    this.createRepresentationInfoRules();
    this.createFileStatusRules();
    this.createMetaDataRules();
  }

  /**
   * Creates rules for the creation of the Element object.
   */
  private void createElementRules() {
    this.digester.addCallMethod("fits", "createElement", 2);
    this.digester.addCallParam("fits/fileinfo/filename", 0);
    this.digester.addCallParam("fits/fileinfo/filepath", 1);
  }

  /**
   * Creates rules for the parsing of the identification data within a FITS
   * file.
   */
  private void createIdentityRules() {
    this.createIdentityStatusRules();

    this.createFormatRule("fits/identification/identity");
    this.createFormatVersionRule("fits/identification/identity/version");
    this.createPuidRule("fits/identification/identity/externalIdentifier");

  }

  /**
   * Creates rules for parsing the identity status data within a FITS file.
   */
  private void createIdentityStatusRules() {
    this.digester.addCallMethod("fits/identification", "setIdentityStatus", 1);
    this.digester.addCallParam("fits/identification", 0, "status");
  }

  /**
   * Creates rules for parsing the format data within a FITS file.
   * 
   * @param pattern
   *          the xpath to the format identity.
   */
  private void createFormatRule(String pattern) {
    this.digester.addCallMethod(pattern, "createIdentity", 2);
    this.digester.addCallParam(pattern, 0, "format");
    this.digester.addCallParam(pattern, 1, "mimetype");

    this.digester.addCallMethod(pattern + "/tool", "addIdentityTool", 2);
    this.digester.addCallParam(pattern + "/tool", 0, "toolname");
    this.digester.addCallParam(pattern + "/tool", 1, "toolversion");

  }

  /**
   * Creates rules for parsing the format version data within a FITS file.
   * 
   * @param pattern
   *          the xpath to the format version identity.
   */
  private void createFormatVersionRule(String pattern) {
    this.digester.addCallMethod(pattern, "createFormatVersion", 4);
    this.digester.addCallParam(pattern, 0);
    this.digester.addCallParam(pattern, 1, "status");
    this.digester.addCallParam(pattern, 2, "toolname");
    this.digester.addCallParam(pattern, 3, "toolversion");
  }

  /**
   * Creates rules for parsing the pronom identifier data within a FITS file.
   * 
   * @param pattern
   *          the xpath to the external identifier.
   */
  private void createPuidRule(String pattern) {
    this.digester.addCallMethod(pattern, "createPuid", 3);
    this.digester.addCallParam(pattern, 0);
    this.digester.addCallParam(pattern, 1, "toolname");
    this.digester.addCallParam(pattern, 2, "toolversion");

  }

  /**
   * Creates rules for the parsing of the file information data within a FITS
   * file.
   */
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

  /*
   * Experimental
   */
  /**
   * This is not part of the original FITS specification, but it is reading out
   * representation information out of RODA, if the FITS was provided by RODA.
   */
  private void createRepresentationInfoRules() {
    this.createValueRule("fits/representationinfo/original");
  }

  /**
   * Creates rules for parsing the file status data within a FITS file.
   */
  private void createFileStatusRules() {
    this.createValueRule("fits/filestatus/well-formed");
    this.createValueRule("fits/filestatus/valid");
    this.createValueRule("fits/filestatus/message");
  }

  /**
   * Creates rules for parsing the meta data section of a FITS file.
   */
  private void createMetaDataRules() {
    this.createValueRule("fits/metadata/image/*");
    this.createValueRule("fits/metadata/text/*");
    this.createValueRule("fits/metadata/document/*");
    this.createValueRule("fits/metadata/audio/*");
    this.createValueRule("fits/metadata/video/*");
  }

  /**
   * Creates rule for parsing generic values from FITS files.
   * 
   * @param pattern
   *          the xpath to the generic meta data node.
   */
  private void createValueRule(String pattern) {
    this.digester.addCallMethod(pattern, "createValue", 5);
    this.digester.addCallParam(pattern, 0);
    this.digester.addCallParam(pattern, 1, "status");
    this.digester.addCallParam(pattern, 2, "toolname");
    this.digester.addCallParam(pattern, 3, "toolversion");
    this.digester.addCallParamPath(pattern, 4);
  }

}
