package com.petpet.c3po;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;

import com.petpet.c3po.api.Call;
import com.petpet.c3po.api.Message;
import com.petpet.c3po.api.utils.ConfigurationException;
import com.petpet.c3po.common.Config;
import com.petpet.c3po.datamodel.DigitalCollection;
import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.db.DBManager;
import com.petpet.c3po.db.PreparedQueries;
import com.petpet.c3po.tools.ProfileGenerator;
import com.petpet.c3po.tools.SimpleGatherer;
import com.petpet.c3po.tools.fits.FITSMetaDataAdaptor;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.Helper;

/**
 * Just for some static experiments
 * 
 */
public class App implements Call {
  private DigitalCollection test;
  private ProfileGenerator gen;

  public static void main(String[] args) {
    Configurator c = new Configurator();
    c.configure();
    App app = new App();
//    app.foldertest();
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
        
        PreparedQueries pq = new PreparedQueries(DBManager.getInstance().getEntityManager());
      gen = new ProfileGenerator(pq);
      Map<String, Object> config = new HashMap<String, Object>();
      config.put(Config.COLLECTION_CONF, pq.getCollectionByName("Test"));
      config.put(Config.EXPANDED_PROPS_CONF, Helper.getPropertiesByNames("format", "puid"));
      gen.addObserver(this);
      gen.configure(config);
      gen.execute();
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }

  private void foldertest() {
    this.test = new DigitalCollection("Test");
    SimpleGatherer g = new SimpleGatherer(new FITSMetaDataAdaptor(), test);
//    g.gather(new File("/home/peter/Desktop/outputtest/"));
     g.gather(new File("/Users/petar/Desktop/fits/235/"));
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
  public void back(Message<?> n) {
    Object data = n.getData();
    if (data != null && data instanceof Document) {
      gen.write((Document) data);
    }

  }
}
