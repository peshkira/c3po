package com.petpet.c3po.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Property;

public class ProfileGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(ProfileGenerator.class);

  private PersistenceLayer persistence;

  public ProfileGenerator(PersistenceLayer persistence) {
    this.persistence = persistence;
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
      File file = new File(path);
      LOG.info("Will create profile in {}", file.getAbsolutePath());
      if (file.getParentFile() != null && !file.getParentFile().exists()) {
        file.getParentFile().mkdirs();

      }

      file.createNewFile();

      XMLWriter writer = new XMLWriter(new FileWriter(path), format);
      writer.write(doc);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Document generateProfile(String collection) {
    Document document = DocumentHelper.createDocument();
    final Element root = this.createRootElement(document, collection);
    final Element partitions = this.createPartitionsElement(root, "mimetype");

    this.createPartitions(collection, partitions);
    this.createElements(collection, root);

    return document;
  }

  private void createElements(String collection, Element root) {

    final BasicDBObject ref = new BasicDBObject();
    final BasicDBObject keys = new BasicDBObject();

    ref.put("collection", collection);
    keys.put("_id", null);
    keys.put("uid", 1);

    final DBCursor cursor = this.persistence.find("elements", ref, keys);

    Element elements = root.addElement("elements").addAttribute("count", cursor.count() + "");

    while (cursor.hasNext()) {
      DBObject element = cursor.next();
      elements.addElement("element").addAttribute("uid", (String) element.get("uid"));
    }

  }

  private void createPartitions(String collection, Element partitions) {
    // select mimetype values in collection
    List<String> mimetypes = this.getMimetypesInCollection(collection);
    System.out.println(Arrays.deepToString(mimetypes.toArray()));
    
    for (String mime : mimetypes) {
      partitions.addElement("partitions").addAttribute("value", mime);
      // add properties per partition
    }
    
    //TODO add representatives per partition
  }

  private List<String> getMimetypesInCollection(String collection) {
    final List<String> res = new ArrayList<String>();
    final Property p = this.persistence.getCache().getProperty("mimetype");
    final DBCollection elements = this.persistence.getDB().getCollection("elements");
    final String map = Constants.HISTOGRAM_MAP.replaceAll("\\{\\}", p.getId());
    final MapReduceCommand cmd = new MapReduceCommand(elements, map, Constants.HISTOGRAM_REDUCE,
        null, OutputType.INLINE, null);

    final MapReduceOutput output = this.persistence.mapreduce("elements", cmd);

    final List<BasicDBObject> results = (List<BasicDBObject>) output.getCommandResult().get("results");
   
    for (BasicDBObject dbo : results) {
      res.add(dbo.getString("_id"));
    }

    return res;
  }

  private Element createRootElement(Document doc, String collection) {
    Element collpro = doc.addElement("profile").addAttribute("version", Constants.PROFILE_FORMAT_VERSION)
        .addAttribute("collection", collection).addAttribute("date", new Date().getTime() + "");
    return collpro;
  }

  private Element createPartitionsElement(Element root, String filter) {
    return root.addElement("partitions").addAttribute("filter", filter);
  }

  private Element createPropertiesElement(Element collection) {
    return collection.addElement("properties");
  }
}
