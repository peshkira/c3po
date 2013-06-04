package com.petpet.c3po.adaptor.rules.drools;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.definition.rule.Rule;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.WorkingMemoryEventListener;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.LogEntry.ChangeType;

public class ElementModificationListener implements WorkingMemoryEventListener {

  private Map<Element, BasicDBObject> memory = new ConcurrentHashMap<Element, BasicDBObject>();
  private LogCollector logCollector;

  public ElementModificationListener(LogCollector logCollector) {
    super();
    this.logCollector = logCollector;
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
    BasicDBObject document = insertedElement.getDocument();
    BasicDBObject metadata = (BasicDBObject) document.get("metadata");

    this.memory.put(insertedElement, metadata);
  }

  private void stopTrackingElement(Element removedElement) {
    this.memory.remove(removedElement);
  }

  private void trackElementUpdate(Element modifiedElement, Rule rule) {
    BasicDBObject document = modifiedElement.getDocument();
    BasicDBObject newMetadata = (BasicDBObject) document.get("metadata");
    BasicDBObject oldMetadata = this.memory.get(modifiedElement);

    for (Entry<String, Object> oldMetadataEntry : oldMetadata.entrySet()) {
      String propertyId = oldMetadataEntry.getKey();
      BasicDBObject propertyData = (BasicDBObject) oldMetadataEntry.getValue();

      BasicDBObject newPropertyData = (BasicDBObject) newMetadata
          .remove(propertyId);
      if (newPropertyData == null) {
        // data is removed
        this.logCollector.debug("|Removed Info: " + propertyId + " - "
            + propertyData);
        modifiedElement.addLog(propertyId, propertyData.toString(),
            ChangeType.IGNORED, rule.getName());
      } else if (!propertyData.equals(newPropertyData)) {
        // data is changed
        this.logCollector.debug("|changed Info: " + propertyId);
        this.logCollector.debug("|   old value: " + propertyData);
        this.logCollector.debug("|   new value: " + newPropertyData);

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

      this.logCollector.debug("|Added Info: " + propertyId + " - "
          + propertyData);

      modifiedElement.addLog(propertyId, "", ChangeType.ADDED, rule.getName());
    }

    // update memory - recreate new Document
    document = modifiedElement.getDocument();
    newMetadata = (BasicDBObject) document.get("metadata");
    this.memory.put(modifiedElement, newMetadata);

  }

}