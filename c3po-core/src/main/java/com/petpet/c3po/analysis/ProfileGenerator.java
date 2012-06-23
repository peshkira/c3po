package com.petpet.c3po.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.petpet.c3po.analysis.mapreduce.HistogrammJob;
import com.petpet.c3po.analysis.mapreduce.NumericAggregationJob;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Property.PropertyType;

public class ProfileGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileGenerator.class);

  private static final String[] PROPERTIES = { "format", "format.version", "puid", "mimetype", "charset", "linebreak", "compressionscheme",
      "creating.os", "byteorder", "compression.scheme", "colorspace", "icc.profile.name", "icc.profile.version" };
  // "creating.application.name"

  private PersistenceLayer persistence;

  public ProfileGenerator(final PersistenceLayer persistence) {
    this.persistence = persistence;
  }

  public void write(final String xml) {
    try {
      final Document doc = DocumentHelper.parseText(xml);
      this.write(doc);

    } catch (final DocumentException e) {
      e.printStackTrace();
    }
  }

  public void write(final Document doc) {
    this.write(doc, "profiles/output.xml");
  }

  public void write(final Document doc, final String path) {
    try {
      final OutputFormat format = OutputFormat.createPrettyPrint();
      final File file = new File(path);

      LOG.info("Will create profile in {}", file.getAbsolutePath());

      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }

      file.createNewFile();

      final XMLWriter writer = new XMLWriter(new FileWriter(path), format);
      writer.write(doc);
      writer.close();

    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public Document generateProfile(final String collection, final String filter) {
    final BasicDBObject ref = new BasicDBObject();
    ref.put("collection", collection);

    final long count = this.persistence.count(Constants.TBL_ELEMENTS, ref);

    final Document document = DocumentHelper.createDocument();
    final Element root = this.createRootElement(document, collection, count);
    final Element partitions = this.createPartitionsElement(root, filter);

    this.createPartitions(collection, partitions, filter);

    return document;
  }

  private Element createRootElement(final Document doc, final String collection, final long count) {
    final Element profile = doc.addElement("profile").addAttribute("version", Constants.PROFILE_FORMAT_VERSION)
        .addAttribute("collection", collection).addAttribute("date", new Date().getTime() + "")
        .addAttribute("count", count + "");

    return profile;
  }

  private Element createPartitionsElement(final Element root, final String filter) {
    return root.addElement("partitions").addAttribute("filter", filter);
  }

  private void createPartitions(final String collection, final Element partitions, final String filter) {
    // select filter values in collection (e.g. mimetype values)
    final Map<String, Long> filterValues = this.getFilterValues(collection, filter);
    final Property f = this.persistence.getCache().getProperty(filter);

    for (final String value : filterValues.keySet()) {
      final Element partition = partitions.addElement("partition").addAttribute("value", value)
          .addAttribute("occurrences", filterValues.get(value) + "");
      final Element properties = this.createPropertiesElement(partition);

      final PropertyAggregation aggr = new PropertyAggregation(collection, f, value);
      this.createProperties(aggr, properties);
      this.createElements(aggr, partition);// TODO make this optional.

    }

    // TODO representatives?
    // shall we add them based on partitions
    // or based on the whole collection?
  }

  private Map<String, Long> getFilterValues(final String collection, final String filter) {
    final Map<String, Long> res = new HashMap<String, Long>();
    final HistogrammJob job = new HistogrammJob(collection, filter);
    final MapReduceOutput output = job.execute();
    final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");

    for (final BasicDBObject dbo : results) {
      res.put(dbo.getString("_id"), dbo.getLong("value"));

    }

    return res;
  }

  private void createProperties(final PropertyAggregation pa, final Element properties) {
    final List<Property> allprops = this.getProperties(this.persistence.findAll(Constants.TBL_PROEPRTIES));
    final BasicDBObject query = new BasicDBObject("_id", null);
    

    for (Property p : allprops) {
      if (!p.getKey().equals(pa.filter.getKey())) {
        final BasicDBObject ref = new BasicDBObject("collection", pa.collection);
        
        ref.put("metadata." + pa.filter.getId() + ".value", pa.value);
        ref.put("metadata." + p.getId() + ".value", new BasicDBObject("$exists", true));

        final int count = this.persistence.find(Constants.TBL_ELEMENTS, ref, query).count();

        if (count != 0) {
          this.createPropertyElement(pa, properties, p, count);
        }
      }
    }
  }

  private Element createPropertiesElement(final Element partition) {
    return partition.addElement("properties");
  }

  private void createElements(final PropertyAggregation aggr, final Element partition) {
    final Element elements = partition.addElement("elements");
    
    final BasicDBObject ref = new BasicDBObject("collection", aggr.collection);
    ref.put("metadata." + aggr.filter.getId() + ".value", aggr.value);
    
    final BasicDBObject keys = new BasicDBObject("_id", null);
    keys.put("uid", 1);
    
    final DBCursor cursor = this.persistence.find(Constants.TBL_ELEMENTS, ref, keys);

    while (cursor.hasNext()) {
      final DBObject element = cursor.next();
      elements.addElement("element").addAttribute("uid", (String) element.get("uid"));
    }

  }

  private void createPropertyElement(final PropertyAggregation pa, final Element properties, final Property p, int count) {
    final Element prop = properties.addElement("property").addAttribute("id", p.getKey())
        .addAttribute("type", p.getType()).addAttribute("count", count + "");

    final PropertyType type = PropertyType.valueOf(p.getType());

    switch (type) {
      case STRING:
        this.processStringProperty(pa, prop, p);
        break;
      case BOOL:
        this.processBoolProperty(pa, prop, p);
        break;
      case INTEGER:
      case FLOAT:
        this.processNumericProperty(pa, prop, p);
        break;
    }
  }

  private void processStringProperty(final PropertyAggregation pa, final Element prop, final Property p) {
    if (pa.filter.getId().equals(p.getId())) {
      // skip the filter...
      return;
    }

    for (final String s : PROPERTIES) {
      if (p.getKey().equals(s)) {

        final String map = Constants.HISTOGRAM_MAP.replaceAll("\\{\\}", p.getId());
        final DBCollection elements = this.persistence.getDB().getCollection(Constants.TBL_ELEMENTS);
        final BasicDBObject query = new BasicDBObject();
        final MapReduceCommand cmd = new MapReduceCommand(elements, map, Constants.HISTOGRAM_REDUCE, null,
            OutputType.INLINE, query);

        query.put("collection", pa.collection);
        query.put("metadata." + pa.filter.getId() + ".value", pa.value);
        query.put("metadata." + p.getId(), new BasicDBObject("$exists", true));
        
        final MapReduceOutput output = this.persistence.mapreduce(Constants.TBL_ELEMENTS, cmd);
        final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");

        Collections.sort(results, new Comparator<BasicDBObject>() {

          @Override
          public int compare(BasicDBObject o1, BasicDBObject o2) {
            final String key = "value";
            final Long l1 = o1.getLong(key);
            final Long l2 = o2.getLong(key);
            return l2.compareTo(l1); // from largest to smallest.
          }

        });

        for (final BasicDBObject dbo : results) {
          prop.addElement("item").addAttribute("id", dbo.getString("_id"))
              .addAttribute("value", dbo.getLong("value") + "");
        }

        break;
      }
    }
  }

  private void processBoolProperty(final PropertyAggregation pa, final Element prop, final Property p) {
    final BasicDBObject query = new BasicDBObject("_id", null);
    final BasicDBObject ref = new BasicDBObject("collection", pa.collection);

    ref.put("metadata." + pa.filter.getId() + ".value", pa.value);
    ref.put("metadata." + p.getId() + ".value", true);

    final int yes = this.persistence.find(Constants.TBL_ELEMENTS, ref, query).count();

    ref.put("metadata." + p.getId() + ".value", false);

    final int no = this.persistence.find(Constants.TBL_ELEMENTS, ref, query).count();

    // String mode = (yes > no) ? "true" : "false";
    // prop.addAttribute("mode", mode);

    prop.addElement("item").addAttribute("value", "true").addAttribute("count", yes + "");
    prop.addElement("item").addAttribute("value", "false").addAttribute("count", no + "");
  }

  private void processNumericProperty(final PropertyAggregation pa, final Element prop, final Property p) {
    final NumericAggregationJob job = new NumericAggregationJob(pa.collection, p, pa.filter.getId(), pa.value);
    final MapReduceOutput output = job.execute();

    final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
    final BasicDBObject aggregation = (BasicDBObject) results.get(0).get("value");

    prop.addAttribute("count", removeTrailingZero(aggregation.getString("count")));
    prop.addAttribute("sum", removeTrailingZero(aggregation.getString("sum")));
    prop.addAttribute("min", removeTrailingZero(aggregation.getString("min")));
    prop.addAttribute("max", removeTrailingZero(aggregation.getString("max")));
    prop.addAttribute("avg", removeTrailingZero(aggregation.getString("avg")));
    prop.addAttribute("var", removeTrailingZero(aggregation.getString("variance")));
    prop.addAttribute("sd", removeTrailingZero(aggregation.getString("stddev")));
  }

  private String removeTrailingZero(final String str) {
    if (str != null && str.endsWith(".0")) {
      return str.substring(0, str.length() - 2);
    }

    return str;
  }

  // TODO find a better place for this and remove it from the CSVGenerator
  /**
   * Extracts {@link Property} objects from the given cursor and only sets the
   * id and the name field.
   * 
   * @param cursor
   *          the cursor to look for property objects.
   * @return a list of properties or an empty list.
   */
  private List<Property> getProperties(final DBCursor cursor) {
    final List<Property> result = new ArrayList<Property>();

    while (cursor.hasNext()) {
      final DBObject next = cursor.next();

      final String id = (String) next.get("_id");
      final String name = (String) next.get("key");
      final String type = (String) next.get("type");

      if (id != null && name != null) {
        final Property p = new Property();
        p.setId(id);
        p.setKey(name);
        p.setType(type);

        result.add(p);
      }
    }

    return result;
  }

  private class PropertyAggregation {
    protected String collection;

    protected Property filter;

    protected String value;

    public PropertyAggregation(final String c, final Property f, final String v) {
      this.collection = c;
      this.filter = f;
      this.value = v;
    }
  }
}
