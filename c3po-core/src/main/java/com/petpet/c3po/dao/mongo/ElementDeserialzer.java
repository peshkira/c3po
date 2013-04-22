package com.petpet.c3po.dao.mongo;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.common.Constants;

public class ElementDeserialzer implements ModelDeserializer {

  // TODO implement properly!!!
  @Override
  public Element deserialize(Object object) {
    DBObject dbObject = (DBObject) object;

    return this.parseElement(dbObject, null);

  }

  /**
   * Parses the element from a db object returned by the db.
   * 
   * @param obj
   *          the object to parse.
   * @return the Element.
   */
  private Element parseElement(final DBObject obj, final PersistenceLayer pl) {
    String coll = (String) obj.get("collection");
    String uid = (String) obj.get("uid");
    String name = (String) obj.get("name");

    Element e = new Element(coll, uid, name);
    e.setId(obj.get("_id").toString());
    e.setMetadata(new ArrayList<MetadataRecord>());

    DBObject meta = (BasicDBObject) obj.get("metadata");
    for (String key : meta.keySet()) {
      MetadataRecord rec = new MetadataRecord();
      DBObject prop = (DBObject) meta.get(key);
      Property p = pl.getCache().getProperty(key);
      rec.setProperty(p);
      rec.setStatus(prop.get("status").toString());

      Object value = prop.get("value");
      if (value != null) {
        rec.setValue(value.toString());
      }

      // because of boolean and other type conversions.
      List<?> tmp = (List) prop.get("values");
      if (tmp != null) {
        List<String> values = new ArrayList<String>();
        for (Object o : tmp) {
          values.add(o.toString());
        }
        rec.setValues(values);
      }

      List<String> src = (List<String>) prop.get("sources");
      if (src != null) {
        List<String> sources = new ArrayList<String>();
        for (String s : src) {
          DBObject next = pl.find(Constants.TBL_SOURCES, new BasicDBObject("_id", s), new BasicDBObject()).next();
          String source = (String) next.get("name") + " " + next.get("version");
          sources.add(source);
        }
        rec.setSources(sources);
      }

      e.getMetadata().add(rec);
    }

    return e;
  }

}
