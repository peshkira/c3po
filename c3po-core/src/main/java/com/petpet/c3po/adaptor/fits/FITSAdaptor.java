package com.petpet.c3po.adaptor.fits;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RegexRules;
import org.apache.commons.digester3.SimpleRegexMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.petpet.c3po.adaptor.AbstractAdaptor;
import com.petpet.c3po.adaptor.rules.PostProcessingRule;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.DigitalObjectStream;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;

public class FITSAdaptor extends AbstractAdaptor {

  private static final Logger LOG = LoggerFactory.getLogger(FITSAdaptor.class);

  private DigitalObjectStream metadata;

  private final Digester digester;

  /**
   * Flag taken from configuration
   * 
   * @see com.petpet.c3po.common.Constants#CNF_INFER_DATE
   */
  private boolean inferDate = false;

  /**
   * The collection to which the processed elements will belong to. Taken from
   * the current configuration.
   */
  private String collection;

  public FITSAdaptor() {
    this.digester = new Digester(); // not thread safe
    this.digester.setRules(new RegexRules(new SimpleRegexMatcher()));
    this.createParsingRules();
  }

  @Override
  public void configure(Map<String, Object> config) {
    this.setConfig(config);

    this.inferDate = this.getBooleanConfig(Constants.CNF_INFER_DATE, false);
    this.collection = this.getStringConfig(Constants.CNF_COLLECTION_ID, AbstractAdaptor.UNKNOWN_COLLECTION_ID);
  }

  private void createParsingRules() {
    this.createElementRules();
    this.createIdentityRules();
    this.createFileInfoRules();
    this.createRepresentationInfoRules();
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

  /**
   * This is not part of the original FITS specification, but it is reading out
   * representation information out of RODA, if the FITS was provided by RODA.
   */
  private void createRepresentationInfoRules() {
    this.createValueRule("fits/representationinfo/original");
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
    if (this.metadata == null) {
      LOG.warn("The input stream is not set, skipping.");
      return null;
    }

    try {
      DigesterContext context = new DigesterContext(this.getController().getPersistence().getCache(),
          this.getPreProcessingRules());
      this.digester.push(context);
      context = (DigesterContext) this.digester.parse(this.metadata.getData());
      final Element element = this.postProcess(context);

      return element;

    } catch (IOException e) {
      LOG.error("An exception occurred while processing {}: {}", this.metadata, e.getMessage());
    } catch (SAXException e) {
      LOG.error("An exception occurred while parsing {}: {}", this.metadata, e.getMessage());
    } finally {
      try {
        this.metadata.getData().close();
      } catch (IOException ioe) {
        LOG.error("An exception occurred while closing {}: {}", this.metadata, ioe.getMessage());
      }
    }

    return null;
  }

  private Element postProcess(DigesterContext context) {
    Element element = context.getElement();
    final List<MetadataRecord> values = context.getValues();

    if (element != null) {
      element.setMetadata(values);
      element.setCollection(this.collection);

      if (this.inferDate) {
        element.extractCreatedMetadataRecord(this.getController().getPersistence().getCache().getProperty("created"));
      }

      // if for some reason there was no uid, set a random one.
      if (element.getUid() == null) {
        element.setUid(UUID.randomUUID().toString());
      }

    }

    List<PostProcessingRule> postProcessingRules = this.getPostProcessingRules();
    for (PostProcessingRule rule : postProcessingRules) {
      element = rule.process(element);
    }

    return element;
  }

  @Override
  public void run() {
    DigitalObjectStream next = this.getController().getNext();

    while (next != null) {
      try {
        this.metadata = next;

        final Element element = this.getElement();
        if (element != null) {

          this.getController().getPersistence().insert(Constants.TBL_ELEMENTS, element.getDocument());

        } else {
          LOG.warn("No element could be extracted for file {}", this.metadata.getFileName());
          // potentially move file to some place for further investigation.
        }

      } catch (Exception e) {
        // save thread from dying due to processing error...
        LOG.warn("An exception occurred for file '{}': {}", this.metadata.getFileName(), e.getMessage());
      }

      next = this.getController().getNext();
    }
  }
}
