package com.petpet.collpro.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.db.PreparedQueries;

public class ProfileGenerator {

  private PreparedQueries queries;

  public ProfileGenerator(PreparedQueries queries) {
    this.queries = queries;
  }

  public Document generateProfile(DigitalCollection coll) {
    Document document = DocumentHelper.createDocument();
    Element collpro = document.addElement("collection-profile").addAttribute("date", new Date().toString());

    List<Property> allProps = this.queries.getAllPropertiesInCollection(coll);
    int props = allProps.size();
    Element collection = collpro.addElement("collection").addAttribute("name", coll.getName())
        .addAttribute("elements", coll.getElements().size() + "").addAttribute("properties", "" + props);

    Element properties = collection.addElement("properties");
    Collections.sort(allProps, new PropertyComparator());

    for (Property p : allProps) {
      System.out.println("adding property " +p.getName());
      List<Object[]> distr = this.queries.getSpecificPropertyValuesDistribution(p.getName(), coll);

      if (!distr.isEmpty()) {
        String mode = (String) distr.get(0)[1];
        long count = this.queries.getElementsWithPropertyCount(p.getName(), coll);
        properties.addElement("property").addAttribute("name", p.getName()).addAttribute("type", p.getType().name())
          .addAttribute("count", count + "")
          .addAttribute("mode", mode)
          .addAttribute("expanded", "false"); // TODO check this later on..
      } else {
        System.out.println("Values for property " + p.getName() + " have conflicts, excluding property...");
      }
    }

    return document;
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
}
