package com.petpet.c3po.adaptor.rules.drools;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.DBObject;
import com.petpet.c3po.dao.mongo.MongoElementSerializer;
import org.drools.definition.rule.Rule;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.WorkingMemoryEventListener;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.LogEntry.ChangeType;

public class ElementModificationListener implements WorkingMemoryEventListener {

  private Map<Element, BasicDBObject> memory = new ConcurrentHashMap<Element, BasicDBObject>();
  private LogCollector logCollector;
  private int loglevel;

  public ElementModificationListener(LogCollector logCollector, int loglevel) {
    super();
    this.logCollector = logCollector;
    this.loglevel = loglevel;
  }

  @Override
  public void objectInserted(ObjectInsertedEvent event) {

    Object insertedObject = event.getObject();
    if (insertedObject instanceof Element) {
      Element insertedElement = (Element) insertedObject;

      // Element was inserted, which has initial metadata
      this.startTrackingElementChanges(insertedElement);
    }
  }

  @Override
  public void objectRetracted(ObjectRetractedEvent event) {
    Object removedObject = event.getOldObject();
    if (removedObject instanceof Element) {
      Element removedElement = (Element) removedObject;

      // Element was removed, so we dont need to track metadata changes
      // anymore.
      this.stopTrackingElement(removedElement);
    }
  }

  @Override
  public void objectUpdated(ObjectUpdatedEvent event) {
    Rule rule = event.getPropagationContext().getRule();

    Object modifiedObject = event.getObject();
    if (modifiedObject instanceof Element) {
      Element modifiedElement = (Element) modifiedObject;

      // rule modified Element, which has (new/updated) metadata
      this.trackElementUpdate(modifiedElement, rule);
    }
  }

  private void startTrackingElementChanges(Element insertedElement) {
    DBObject document = new MongoElementSerializer().serialize(insertedElement);
    BasicDBObject metadata = (BasicDBObject) document.get("metadata");

    this.memory.put(insertedElement, metadata);
  }

  private void stopTrackingElement(Element removedElement) {
    this.memory.remove(removedElement);
  }

  private void trackElementUpdate(Element modifiedElement, Rule rule) {
    DBObject document = new MongoElementSerializer().serialize(modifiedElement);
    BasicDBObject newMetadata = (BasicDBObject) document.get("metadata");
    BasicDBObject oldMetadata = this.memory.get(modifiedElement);

    for (Entry<String, Object> oldMetadataEntry : oldMetadata.entrySet()) {
      String propertyId = oldMetadataEntry.getKey();
      BasicDBObject propertyData = (BasicDBObject) oldMetadataEntry.getValue();

      BasicDBObject newPropertyData = (BasicDBObject) newMetadata
          .remove(propertyId);
      if (newPropertyData == null) {
        // data is removed
        this.logCollector.log(this.loglevel, "|Removed Info: " + propertyId
            + " - " + propertyData);
        modifiedElement.addLog(propertyId, propertyData.toString(),
            ChangeType.IGNORED, rule.getName());
      } else if (!propertyData.equals(newPropertyData)) {
        // data is changed
        this.logCollector.log(this.loglevel, "|changed Info: " + propertyId);
        this.logCollector.log(this.loglevel, "|   old value: " + propertyData);
        this.logCollector.log(this.loglevel, "|   new value: "
            + newPropertyData);

        modifiedElement.addLog(propertyId, propertyData.toString(),
            ChangeType.UPDATED, rule.getName());

      } else {
        // data is unchanged
      }
    }

    for (Entry<String, Object> newMetadataEntry : newMetadata.entrySet()) {
      // property was added
      String propertyId = newMetadataEntry.getKey();
      Object propertyData = newMetadataEntry.getValue();

      this.logCollector.log(this.loglevel, "|Added Info: " + propertyId + " - "
          + propertyData);

      modifiedElement.addLog(propertyId, "", ChangeType.ADDED, rule.getName());
    }

    // update memory - recreate new Document
    document = new MongoElementSerializer().serialize(modifiedElement);
    newMetadata = (BasicDBObject) document.get("metadata");
    this.memory.put(modifiedElement, newMetadata);

  }

}