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
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataStream;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.common.Constants;

public class TIKAAdaptor extends AbstractAdaptor {

  private static final Logger LOG = LoggerFactory.getLogger(TIKAAdaptor.class);

  private String collection;

  public TIKAAdaptor() {

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
      key = key.replace('.', '_');
      key = TIKAHelper.getPropertyKeyByTikaName(key);

      if (key != null) {
        Property prop = cache.getProperty(key);
        MetadataRecord record = new MetadataRecord(prop, value);
        Source source = cache.getSource("Tika", "");
        record.setSources(Arrays.asList(source.getId()));
        records.add(record);
      }
    }

    element.setMetadata(records);
    element.setCollection(this.collection);
    return element;

  }

  @Override
  public void configure() {
    this.collection = this.getStringConfig(Constants.CNF_COLLECTION_ID, AbstractAdaptor.UNKNOWN_COLLECTION_ID);
  }

  // TODO implement properly.
  @Override
  public Element parseElement(MetadataStream ms) {
    try {
      Map<String, String> metadata = TIKAResultParser.getKeyValueMap(ms.getData());
      Element element = this.createElement(metadata);
      return element;
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public String getAdaptorPrefix() {
    // TODO Auto-generated method stub
    return null;
  }

}
