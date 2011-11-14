package com.petpet.collpro.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.api.ITool;
import com.petpet.collpro.api.Notification;
import com.petpet.collpro.api.NotificationListener;
import com.petpet.collpro.api.utils.ConfigurationException;
import com.petpet.collpro.common.Config;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.db.PreparedQueries;

public class ProfileGenerator implements ITool {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileGenerator.class);

  private DigitalCollection coll;

  private PreparedQueries queries;

  private Set<NotificationListener> observers;

  private List<Property> expanded;

  public ProfileGenerator(PreparedQueries queries) {
    this.queries = queries;
    this.observers = new HashSet<NotificationListener>();
  }

  public void write(Document doc) {
    try {
      OutputFormat format = OutputFormat.createPrettyPrint();
      XMLWriter writer = new XMLWriter(new FileWriter("output.xml"), format);
      writer.write(doc);
      writer.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void addObserver(NotificationListener listener) {
    if (!this.observers.contains(listener)) {
      this.observers.add(listener);
    }
  }

  @Override
  public void removeObserver(NotificationListener listener) {
    this.observers.remove(listener);

  }

  @Override
  public void notifyObservers(Object data) {
    for (NotificationListener l : this.observers) {
      Notification<Document> n = new Notification<Document>((Document) data);
      l.notify(n);
    }
  }

  @Override
  public void execute() {
    Document profile = this.generateProfile(coll);
    this.notifyObservers(profile);

  }

  @Override
  public void configure(Map<String, Object> configuration) throws ConfigurationException {
    this.coll = (DigitalCollection) configuration.get(Config.COLLECTION_CONF);
    this.expanded = (List<Property>) configuration.get(Config.EXPANDED_PROPS_CONF);

    if (this.coll == null) {
      throw new ConfigurationException(
          "No collection was passed, please pass a collection for which a profile will be generated");
    }

    if (this.expanded == null) {
      LOG.warn("No properties were passed for expansion, assuming false for all properties.");
      this.expanded = new ArrayList<Property>();
    }

  }

  private Document generateProfile(DigitalCollection coll) {
    LOG.info("generating profile for collection '{}'", coll.getName());
    Document document = DocumentHelper.createDocument();
    Element collpro = document.addElement("collection-profile").addAttribute("date", new Date().toString());

    List<Property> allProps = this.queries.getAllPropertiesInCollection(coll);
    int props = allProps.size();
    Element collection = collpro.addElement("collection").addAttribute("name", coll.getName())
        .addAttribute("elements", coll.getElements().size() + "").addAttribute("properties", "" + props);

    Element properties = collection.addElement("properties");
    Collections.sort(allProps, new PropertyComparator());

    for (Property p : allProps) {
      LOG.debug("adding property {} to profile", p.getName());
      List<Object[]> distr = this.queries.getSpecificPropertyValuesDistribution(p.getName(), coll);

      if (!distr.isEmpty()) {
        String mode = (String) distr.get(0)[1];
        long count = this.queries.getElementsWithPropertyCount(p.getName(), coll);
        Element property = properties.addElement("property").addAttribute("id", p.getName())
            .addAttribute("name", p.getHumanReadableName()).addAttribute("type", p.getType().name())
            .addAttribute("count", count + "").addAttribute("mode", mode);

        if (this.expanded.contains(p)) {
          property.addAttribute("expanded", "true");

          for (Object[] d : distr) {
            property.addElement("value").addAttribute("value", (String) d[1])
                .addAttribute("count", ((Long) d[2]).toString());
          }

        } else {
          property.addAttribute("expanded", "false");
        }

      } else {
        LOG.warn("Values for property '{}' have conflicts, excluding property...", p.getName());
      }
    }

    return document;
  }

}
