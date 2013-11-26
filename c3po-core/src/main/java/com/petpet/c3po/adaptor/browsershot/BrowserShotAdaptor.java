/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.petpet.c3po.adaptor.browsershot;

import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author artur
 */
public class BrowserShotAdaptor extends AbstractAdaptor {

  private static final Logger LOG = LoggerFactory.getLogger(BrowserShotAdaptor.class);

  @Override
  public void configure() {
    //what should be done ? ))
  }

  @Override
  public String getAdaptorPrefix() {
    return "browsershot";
  }

  @Override
  public Element parseElement(String name, String data) {
    try {
      List<MetadataRecord> records = BrowserShotParser.parse(data);
      Element element = new Element(name, name);
      element.setMetadata(records);
      return process(element);
    } catch (Exception e) {
      LOG.warn("Could not parse data for {}", name);
      return null;
    }
  }
  public Element process(Element e) {
		List<MetadataRecord> BrowsershotRecords = e.getMetadata();
		int total=0;
		int negative=0;
		for ( MetadataRecord mr : BrowsershotRecords ) {
			if (mr.getProperty().getType()=="FLOAT"){
				total++;
				Float value = Float.parseFloat(mr.getValue());
				if (value<0)
					negative++;
			}
		}
		Property p = new Property("dissimilarities", PropertyType.STRING);
		MetadataRecord mr = new MetadataRecord(p,String.valueOf(negative));
		e.getMetadata().add(mr);
		return e;
	}
}
