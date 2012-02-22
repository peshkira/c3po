package com.petpet.c3po;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.Persistence;

import org.dom4j.Document;

import com.petpet.c3po.api.Call;
import com.petpet.c3po.api.Message;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.utils.ConfigurationException;
import com.petpet.c3po.common.Config;
import com.petpet.c3po.controller.GathererController;
import com.petpet.c3po.controller.ProfileGenerator;
import com.petpet.c3po.datamodel.C3POConfig;
import com.petpet.c3po.datamodel.C3POConfig.GathererType;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.db.DBManager;
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
    app.pl = new LocalPersistenceLayer(Persistence.createEntityManagerFactory("LocalC3POPersistenceUnit"));

    Configurator c = new Configurator(app.pl);
    c.configure();

    app.foldertest();
    // app.querytest();
    app.genprofile();
  }

  private void genprofile() {
    try {
      // List<Property> props = Helper.getPropertiesByNames(new String[] {
      // "apertureValue", "avgBitRate", "avgPacketSize",
      // "bitDepth", "bitRate", "bitsPerSample", "blockAlign", "blockSizeMax",
      // "blockSizeMin", "brightnessValue",
      // "byteOrder", "captureDevice", "channels", "charset", "colorSpace",
      // "compressionScheme",
      // "digitalCameraManufacturer", "digitalCameraModelName", "duration",
      // "exifVersion", "exposureBiasValue",
      // "exposureProgram", "exposureTime", "flash", "fNumber", "focalLength",
      // "iccProfileName", "iccProfileVersion",
      // "imageHeight", "imageWidth", "inhibitorType", "isoSpeedRating",
      // "lightSource", "linebreak", "markupBasis",
      // "maxApertureValue", "maxBitRate", "maxPacketSize", "meteringMode",
      // "numPackets", "numSamples", "offset",
      // "orientation", "sampleRate", "samplesPerPixel",
      // "samplingFrequencyUnit", "scannerManufacturer",
      // "scannerModelName", "scanningSoftwareName", "sensingMethod",
      // "shutterSpeedValue", "wordSize",
      // "xSamplingFrequency", "YCbCrPositioning", "YCbCrSubSampling",
      // "ySamplingFrequency" });

      PreparedQueries pq = new PreparedQueries(pl.getEntityManager());
      gen = new ProfileGenerator(pq);
      Map<String, Object> config = new HashMap<String, Object>();
      config.put(Config.COLLECTION_CONF, "Test");
      config.put(Config.EXPANDED_PROPS_CONF, Arrays.asList("format", "puid").toArray());
      gen.addObserver(this);
      gen.configure(config);
      gen.execute();
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }

  private void foldertest() {
    Map<String, String> c = new HashMap<String, String>();
    c.put(C3POConfig.LOCATION, "/Users/petar/Desktop/misc/fits/");
    c.put(C3POConfig.NAME, "LocalFileSystem config");

    C3POConfig conf = new C3POConfig();
    conf.setType(GathererType.FS);
    conf.setConfigs(c);

    this.test = new DigitalCollection("Test");
    this.test.setConfigurations(new HashSet<C3POConfig>(Arrays.asList(conf)));

    pl.handleCreate(DigitalCollection.class, this.test);

    GathererController controller = new GathererController(pl, test, new Date());
    controller.collectMetaData();

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
