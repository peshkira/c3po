package com.petpet.c3po;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.dom4j.Document;

import com.petpet.c3po.api.Call;
import com.petpet.c3po.api.Message;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.controller.GathererController;
import com.petpet.c3po.controller.ProfileGenerator;
import com.petpet.c3po.dao.LocalPersistenceLayer;
import com.petpet.c3po.datamodel.C3POConfig;
import com.petpet.c3po.datamodel.C3POConfig.GathererType;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Element;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.datamodel.Value;
import com.petpet.c3po.db.PreparedQueries;
import com.petpet.c3po.utils.Configurator;

/**
 * Just for some static experiments
 * 
 */
public class App implements Call {
  private DigitalCollection test;
  private ProfileGenerator gen;
  private PersistenceLayer pl;

  public static void main(String[] args) {
    App app = new App();
    app.pl = new LocalPersistenceLayer();

    Configurator c = new Configurator(app.pl);
    c.configure();

    long start = System.currentTimeMillis();
    app.foldertest();
//     app.genprofile();
//     app.filter();
//    app.matrix();
    long end = System.currentTimeMillis();

    System.out.println("Time: " + (end - start));
    // app.querytest();
  }

  private void filter() {
    PreparedQueries pq = new PreparedQueries(pl.getEntityManager());
    List<Element> list = pq.getElementsWithinDoubleFilteredCollection("format", "Portable Document Format", "format.version", "1.6", this.getCollection("FAO"));
    System.out.println(list.size());
    
    for (Element e : list) {
      System.out.println(e.getName());
    }
    
  }
  
  private void matrix() {
    BufferedWriter writer = null;
    try {
    writer = new BufferedWriter(new FileWriter(new File("output2.csv")), 32768);
    PreparedQueries pq = new PreparedQueries(pl.getEntityManager());
    DigitalCollection collection = this.getCollection("FAO");
    List<Property> props = pq.getAllPropertiesInCollection(collection);
    String header = "element";
    for (Property s : props) {
      header += (", " + s.getName());
    }
    writer.write(header);
    writer.newLine();
    List<Element> elements = pl.getEntityManager().createQuery("FROM Element WHERE id < 1000", Element.class).getResultList();
    String[] values;
    int i = 0;
    for (Element e : elements) {
      values = new String[props.size() + 1];
      values[0] = e.getName();
//      e = pl.getEntityManager().merge(e);
      for (Value v : e.getValues()) {
        if (!v.getStatus().equals("CONFLICT")) {
          values[props.indexOf(v.getProperty()) + 1] = ", " + v.getValue().replaceAll(",", "").replaceAll("\n", "");
        }
      }
      
      String line = "";
      for (String s : values) {
        line += (s == null ? ", " : s);
      }
      writer.write(line);
      writer.newLine();
      i++;
      
      if (i % 100 == 0) {
        writer.flush();
        System.out.println("Finished next " + i);
//        pl.getEntityManager().clear();
//        pl.getEntityManager().merge(collection);
      }
    }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        writer.flush();
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
  }

  private void genprofile() {
    PreparedQueries pq = new PreparedQueries(pl.getEntityManager());
    ProfileGenerator gen = new ProfileGenerator("FAO", Arrays.asList("format", "puid", "mimetype"), pq);
    Document profile = gen.generateProfile();
    gen.write(profile);
  }

  private void foldertest() {
    Map<String, String> c = new HashMap<String, String>();
    c.put(C3POConfig.LOCATION, "/Users/petar/Downloads/fao");//done
    c.put(C3POConfig.NAME, "LocalFileSystem config");

    C3POConfig conf = new C3POConfig();
    conf.setType(GathererType.FS);
    conf.setConfigs(c);

    this.test = this.getCollection("FAO2");
    this.test.setConfigurations(new HashSet<C3POConfig>(Arrays.asList(conf)));
    pl.handleUpdate(DigitalCollection.class, this.test);

    GathererController controller = new GathererController(pl, test, new Date());
    controller.collectMetaData();

  }

  private DigitalCollection getCollection(String name) {
    PreparedQueries pq = new PreparedQueries(pl.getEntityManager());

    DigitalCollection collection = null;
    try {
      collection = pq.getCollectionByName(name);
    } catch (NoResultException e) {
      //swallow
    }

    if (collection == null) {
      collection = new DigitalCollection(name);
      pl.handleCreate(DigitalCollection.class, collection);
    }

    return collection;

  }

  //
  // private void querytest() {
  // PreparedQueries analyzer = new
  // PreparedQueries(DBManager.getInstance().getEntityManager());
  // System.out.println("QUERIES");
  //
  // this.test = analyzer.getCollectionByName("Test");
  //
  // List<Property> allprops = analyzer.getAllPropertiesInCollection(test);
  // System.out.println("PROPS IN COLLECTION");
  // for (Property p : allprops) {
  // System.out.println(p.getName());
  // }
  //
  // Long count = analyzer.getElementsWithPropertyAndValueCount("mimetype",
  // "application/pdf", test);
  // System.out.println("PDFs: " + count);
  //
  // count = analyzer.getDistinctPropertyValueCount("mimetype", test);
  // System.out.println("Distinct mimetypes: " + count);
  //
  // List<String> list = analyzer.getDistinctPropertyValueSet("mimetype", test);
  // for (String v : list) {
  // System.out.println("mimetype: " + v);
  // }
  //
  // List res = analyzer.getMostOccurringProperties(5, test);
  // System.out.println("GET MOST OCCURRING PROPERTIES");
  // for (Object o : res) {
  // Object[] p = (Object[]) o;
  // System.out.println(Arrays.deepToString(p));
  // }
  //
  // Long sum = analyzer.getSumOfNumericProperty("size", test);
  // System.out.println("All elements size " + sum);
  //
  // Double avg = analyzer.getAverageOfNumericProperty("size", test);
  // System.out.println("AVG elements size " + avg);
  //
  // res = analyzer.getValuesDistribution(test);
  // System.out.println("DISTRIBUTION");
  // for (Object o : res) {
  // Object[] p = (Object[]) o;
  // System.out.println(Arrays.deepToString(p));
  // }
  //
  // res = analyzer.getSpecificPropertyValuesDistribution("format", test);
  // System.out.println("Specific DISTRIBUTION");
  // for (Object o : res) {
  // Object[] p = (Object[]) o;
  // System.out.println(Arrays.deepToString(p));
  // }
  //
  // }
  //
  @Override
  public void back(Message<?> n) {
    Object data = n.getData();
    if (data != null && data instanceof Document) {
      gen.write((Document) data);
    }

  }
}
