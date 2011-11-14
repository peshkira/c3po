package com.petpet.collpro;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.Document;

import com.petpet.collpro.api.Notification;
import com.petpet.collpro.api.NotificationListener;
import com.petpet.collpro.api.utils.ConfigurationException;
import com.petpet.collpro.common.Config;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.db.DBManager;
import com.petpet.collpro.db.PreparedQueries;
import com.petpet.collpro.tools.FITSMetaDataConverter;
import com.petpet.collpro.tools.ProfileGenerator;
import com.petpet.collpro.tools.SimpleGatherer;
import com.petpet.collpro.utils.Configurator;

/**
 * Just for some static experiments
 * 
 */
public class App implements NotificationListener {
  private DigitalCollection test;
  private ProfileGenerator gen;

  public static void main(String[] args) {
    Configurator.getInstance().configure();
    App app = new App();
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
      gen = new ProfileGenerator(new PreparedQueries(DBManager.getInstance().getEntityManager()));
      Map<String, Object> config = new HashMap<String, Object>();
      config.put(Config.COLLECTION_CONF, this.test);
      config.put(Config.EXPANDED_PROPS_CONF, null);
      gen.addObserver(this);
      gen.configure(config);
      gen.execute();
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }

  private void foldertest() {
    this.test = new DigitalCollection("Test");
    SimpleGatherer g = new SimpleGatherer(new FITSMetaDataConverter(), test);
    g.gather(new File("/home/peter/Desktop/outputtest/"));
    // g.gather(new File("/Users/petar/Desktop/output/"));
  }

  private void querytest() {
    PreparedQueries analyzer = new PreparedQueries(DBManager.getInstance().getEntityManager());
    System.out.println("QUERIES");

    List<Property> allprops = analyzer.getAllPropertiesInCollection(test);
    System.out.println("PROPS IN COLLECTION");
    for (Property p : allprops) {
      System.out.println(p.getName());
    }

    Long count = analyzer.getElementsWithPropertyAndValueCount("mimetype", "application/pdf", test);
    System.out.println("PDFs: " + count);

    count = analyzer.getDistinctPropertyValueCount("mimetype", test);
    System.out.println("Distinct mimetypes: " + count);

    List<String> list = analyzer.getDistinctPropertyValueSet("mimetype", test);
    for (String v : list) {
      System.out.println("mimetype: " + v);
    }

    List res = analyzer.getMostOccurringProperties(5, test);
    System.out.println("GET MOST OCCURRING PROPERTIES");
    for (Object o : res) {
      Object[] p = (Object[]) o;
      System.out.println(Arrays.deepToString(p));
    }

    Long sum = analyzer.getSumOfNumericProperty("size", test);
    System.out.println("All elements size " + sum);

    Double avg = analyzer.getAverageOfNumericProperty("size", test);
    System.out.println("AVG elements size " + avg);

    res = analyzer.getValuesDistribution(test);
    System.out.println("DISTRIBUTION");
    for (Object o : res) {
      Object[] p = (Object[]) o;
      System.out.println(Arrays.deepToString(p));
    }

    res = analyzer.getSpecificPropertyValuesDistribution("format", test);
    System.out.println("Specific DISTRIBUTION");
    for (Object o : res) {
      Object[] p = (Object[]) o;
      System.out.println(Arrays.deepToString(p));
    }

  }

  @Override
  public void notify(Notification<?> n) {
    Object data = n.getData();
    if (data != null && n.getClazz().equals(Document.class)) {
      gen.write((Document) data);
    }

  }
}
