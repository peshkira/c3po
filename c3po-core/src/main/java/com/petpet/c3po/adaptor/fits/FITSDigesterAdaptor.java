package com.petpet.c3po.adaptor.fits;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.ObjectCreateRule;
import org.apache.commons.digester3.RegexRules;
import org.apache.commons.digester3.SimpleRegexMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.petpet.c3po.controller.GathererController;
import com.petpet.c3po.datamodel.BooleanValue;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.IntegerValue;
import com.petpet.c3po.datamodel.PropertyType;
import com.petpet.c3po.datamodel.StringValue;
import com.petpet.c3po.datamodel.Value;
import com.petpet.c3po.datamodel.ValueSource;
import com.petpet.c3po.utils.Helper;
import com.petpet.c3po.utils.XMLUtils;

public class FITSDigesterAdaptor implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(FITSDigesterAdaptor.class);

  private GathererController controller;

  private InputStream stream;

  private Digester digester;

  private ObjectCreateRule createIntegerValue;

  private ObjectCreateRule createStringValue;

  private ObjectCreateRule createValueSource;

  private ObjectCreateRule createBooleanValue;

  private AttachPropertyRule genericAttachProperty;

  public FITSDigesterAdaptor(GathererController controller) {
    this.controller = controller;
    this.digester = new Digester(); // not thread safe
    this.digester.setRules(new RegexRules(new SimpleRegexMatcher()));
    this.createRules();
  }

  // TODO create identity rules...
  private void createRules() {
    this.createCommonRules();
    this.createElementRules();
    this.createIdentityRules();
    this.createFileInfoRules();
    this.createFileStatusRules();
    this.createMetaDataRules();
  }

  private void createCommonRules() {
    this.createIntegerValue = new ObjectCreateRule(IntegerValue.class);
    this.createStringValue = new ObjectCreateRule(StringValue.class);
    this.createBooleanValue = new ObjectCreateRule(BooleanValue.class);
    this.createValueSource = new ObjectCreateRule(ValueSource.class);
    this.genericAttachProperty = new AttachPropertyRule(null);

    this.createIntegerValue.setConstructorArgumentTypes(Long.class);
    this.createStringValue.setConstructorArgumentTypes(String.class);
    this.createBooleanValue.setConstructorArgumentTypes(String.class);
    this.createValueSource.setConstructorArgumentTypes(String.class, String.class);
  }

  private void createElementRules() {
    this.digester.addObjectCreate("fits", Element.class);
    this.digester.addBeanPropertySetter("fits/fileinfo/filename", "name");
    this.digester.addBeanPropertySetter("fits/fileinfo/filepath", "uid");
  }

  private void createIdentityRules() {
    this.createFitsIdentityRule("fits/identification/identity", "format", new ObjectCreateRule(StringValue.class));
    this.createFitsIdentityRule("fits/identification/identity", "mimetype", new ObjectCreateRule(StringValue.class));
//    this.createFitsPropertyRule("fits/identification/identity/version", this.createStringValue);
    this.createFitsIdentityDetailRule("fits/identification/identity/version", "format.version", this.createStringValue);
    this.createFitsIdentityDetailRule("fits/identification/identity/externalIdentifier", "puid", this.createStringValue);
  }

  private void createFileInfoRules() {
    this.createFitsPropertyRule("fits/fileinfo/size", createIntegerValue);
    this.createFitsPropertyRule("fits/fileinfo/md5checksum", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/lastmodified", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/fslastmodified", createIntegerValue);
    this.createFitsPropertyRule("fits/fileinfo/created", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/creatingApplicationName", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/creatingApplicationVersion", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/inhibitorType", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/inhibitorTarget", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/rightsBasis", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/copyrightBasis", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/copyrightNote", createStringValue);
    this.createFitsPropertyRule("fits/fileinfo/creatingos", createStringValue);
  }

  private void createFileStatusRules() {
    this.createFitsPropertyRule("fits/filestatus/well-formed", createBooleanValue);
    this.createFitsPropertyRule("fits/filestatus/valid", createBooleanValue);
  }

  private void createMetaDataRules() {
    this.createFitsPropertyRule("fits/metadata/image/*", createStringValue);
    this.createFitsPropertyRule("fits/metadata/text/*", createStringValue);
    this.createFitsPropertyRule("fits/metadata/document/*", createStringValue);
    this.createFitsPropertyRule("fits/metadata/audio/*", createStringValue);
    this.createFitsPropertyRule("fits/metadata/video/*", createStringValue);

  }

  private void createFitsPropertyRule(String pattern, ObjectCreateRule rule) {
    this.digester.addRule(pattern, rule); // create object when match found
    this.digester.addCallParam(pattern, 0);// pass xml value as arg to constr.
    this.digester.addSetProperties(pattern, "status", "status"); // set the status
    this.digester.addSetNext(pattern, "addValue"); // add the value.

    this.digester.addRule(pattern, this.genericAttachProperty);

//    this.digester.addRule(pattern, createValueSource);
//    this.digester.addSetProperties(pattern, "toolname", "name");
//    this.digester.addSetProperties(pattern, "toolversion", "version");
//    this.digester.addSetNext(pattern, "setSource");

  }

  private void createFitsIdentityRule(String pattern, String prop, ObjectCreateRule rule) {
    this.digester.addRule(pattern, rule);
    this.digester.addRule(pattern, new AttachPropertyRule(prop));
    this.digester.addSetProperties(pattern, prop, "value");
//    this.digester.addSetProperties("fits/identification", "status", "status");

  }
  
  private void createFitsIdentityDetailRule(String pattern, String prop, ObjectCreateRule rule) {
    this.digester.addRule(pattern, rule);
    this.digester.addCallParam(pattern, 0);
    this.digester.addSetProperties(pattern, "status", "status");
    this.digester.addRule(pattern, new AttachPropertyRule(prop));
  }

  public Element getElement() {
    if (this.stream == null) {
      LOG.warn("The input stream is not set, skipping.");
      return null;
    }

    try {

      Element e = (Element) this.digester.parse(this.stream);
      this.postProcess(e);
      return e;
      
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

  // TODO inspect why the generated objects
  // by the digester are proxies and how can this behaviour can be circumvented.
  // This should optimize the process a bit more.

  private void postProcess(Element e) {
    LOG.debug("Postprocessing element {}", e.getName());
    List<Value<?>> values = new ArrayList<Value<?>>();
    for (Value<?> v : e.getValues()) {
      PropertyType type = v.getProperty().getType();
      LOG.debug("Fixing value of property {}", v.getProperty().getName());

      Value<?> value = Helper.getTypedValue(type.getClazz(), v.getValue());
      LOG.debug("Created new generic value of type {} with value {}", type.name(), value.getValue());
      value.setStatus(v.getStatus());
      value.setSource(v.getSource());
      value.setProperty(v.getProperty());
      value.setElement(v.getElement());
      values.add(value);
    }

    e.setValues(values);
  }

  @Override
  public void run() {
    Element element = this.getElement();
    if (element != null) {
      this.controller.processElement(element);

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

  public static void main(String[] args) {
    try {
      XMLUtils.init();
      Helper.init();
      FITSHelper.init();

      FITSDigesterAdaptor adaptor = new FITSDigesterAdaptor(null);
      // 000000.swf.
      adaptor.setStream(new FileInputStream("src/main/resources/fits.xml"));
      Element element = adaptor.getElement();

      if (element != null) {

        System.out.println("element: " + element.getName() + " " + element.getUid());
        System.out.println("values: " + element.getValues().size());
        for (Value v : element.getValues()) {
          System.out.println("v: " + v.getValue() + " status: " + v.getStatus() + " " + v.getSource() + " "
              + v.getProperty() + " " + v.getClass().toString());
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

}
