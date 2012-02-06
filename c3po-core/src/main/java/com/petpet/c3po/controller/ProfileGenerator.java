package com.petpet.c3po.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.Call;
import com.petpet.c3po.api.ITool;
import com.petpet.c3po.api.Message;
import com.petpet.c3po.api.utils.ConfigurationException;
import com.petpet.c3po.common.Config;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.db.PreparedQueries;
import com.petpet.c3po.utils.Helper;
import com.petpet.c3po.utils.PropertyComparator;

public class ProfileGenerator implements ITool {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileGenerator.class);

    private DigitalCollection coll;

    private PreparedQueries queries;

    private Set<Call> observers;

    private List<Property> expanded;

    public ProfileGenerator(PreparedQueries queries) {
        this.queries = queries;
        this.observers = new HashSet<Call>();
    }

    public void write(String xml) {
        try {
            Document doc = DocumentHelper.parseText(xml);
            this.write(doc);

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
    

    public void write(Document doc) {
        this.write(doc, "profiles/output.xml");
    }
    
    public void write(Document doc, String path) {
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(new FileWriter("path"), format);
            writer.write(doc);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addObserver(Call listener) {
        if (!this.observers.contains(listener)) {
            this.observers.add(listener);
        }
    }

    @Override
    public void removeObserver(Call listener) {
        this.observers.remove(listener);

    }

    @Override
    public void notifyObservers(Object data) {
        final Message<Document> n = new Message<Document>((Document) data);

        for (Call call : this.observers) {
            call.back(n);
        }
    }

    public Document generateProfile() {
        if (this.coll != null) {
            return this.generateProfile(coll);
        } else {
            LOG.error("No collection was provided, aborting... Please configure the tool before execution");
        }
        
        return null;
    }

    @Override
    public void execute() {
        if (this.coll != null) {
            final Document profile = this.generateProfile(coll);
            this.notifyObservers(profile);
        } else {
            LOG.error("No collection was provided, aborting... Please configure the tool before execution");
        }

    }

    @Override
    public void configure(Map<String, Object> configuration) throws ConfigurationException {
        this.coll = this.queries.getCollectionByName((String) configuration.get(Config.COLLECTION_CONF));
        this.expanded = Helper.getPropertiesByNames((String[]) configuration.get(Config.EXPANDED_PROPS_CONF));

        if (this.coll == null) {
            throw new ConfigurationException(
                    "No collection was passed, please pass a collection for which a profile will be generated");
        }

        if (this.expanded == null) {
            LOG.warn("No properties were passed for expansion, assuming false for all properties.");
            this.expanded = new ArrayList<Property>();
        }

    }

    @Override
    public ITool addParameter(String key, Object value) {
        try {

            if (key.equals(Config.COLLECTION_CONF)) {
                this.coll = (DigitalCollection) value;
            } else if (key.equals(Config.EXPANDED_PROPS_CONF)) {
                this.expanded = (List<Property>) value;
            } else {
                LOG.warn("Unknown config param '{}', skipping", key);
            }

        } catch (ClassCastException e) {
            LOG.warn("Unknown data type for key '{}': {}", key, e.getMessage());
        }

        return this;
    }

    @Override
    public List<String> getConfigParameters() {
        return Arrays.asList(Config.COLLECTION_CONF, Config.EXPANDED_PROPS_CONF);
    }

    @Override
    public List<String> getMandatoryParameters() {
        return Arrays.asList(Config.COLLECTION_CONF);
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
                        property.addElement("item").addAttribute("value", (String) d[1])
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
