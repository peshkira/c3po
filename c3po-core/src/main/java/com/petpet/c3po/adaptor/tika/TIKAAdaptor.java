package com.petpet.c3po.adaptor.tika;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.dao.Cache;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.DigitalObjectStream;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.MetadataRecord;
import com.petpet.c3po.datamodel.Property;

public class TIKAAdaptor extends AbstractAdaptor {
  
  private static final Logger LOG = LoggerFactory.getLogger(TIKAAdaptor.class);
  
  private String collection;
  
  public TIKAAdaptor() {
    
  }

  @Override
  public void run() {
    DigitalObjectStream object = this.getController().getNext();

    while (object != null) {
      try {
        Map<String, String> metadata = TIKAResultParser.KeyValueMap(object.getData());
        Element element = this.createElement(metadata);
        
        if (element != null) {
          this.getController().getPersistence().insert(Constants.TBL_ELEMENTS, element.getDocument());
          LOG.info("Storing file");
        } else {
          LOG.error("Could not parse element from {}", object.getFileName());
        }
        
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      object = this.getController().getNext();
    }
  }

  private Element createElement(Map<String, String> metadata) {
    
    String name = metadata.remove("resourceName");
    if (name == null) {
      return null;
    }
    
    Element element = new Element(name, name);
    Cache cache = this.getController().getPersistence().getCache();
    List<MetadataRecord> records = new ArrayList<MetadataRecord>();
    
    for (String key : metadata.keySet()) {
      String value = metadata.get(key);
      Property prop = cache.getProperty(TIKAHelper.getPropertyKeyByTikaName(key));
      MetadataRecord record = new MetadataRecord(prop, value);
      record.setSources(Arrays.asList("Tika"));
      records.add(record);
    }
    
    element.setMetadata(records);
    element.setCollection(this.collection);
    return element;
    
  }
  
  @Override
  public void configure(Map<String, Object> config) {
    this.setConfig(config);
    this.collection = this.getStringConfig(Constants.CNF_COLLECTION_ID, AbstractAdaptor.UNKNOWN_COLLECTION_ID);
  }

}
