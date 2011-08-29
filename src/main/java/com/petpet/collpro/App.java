package com.petpet.collpro;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.petpet.collpro.analyzer.CollectionProfileQueries;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.tools.FITSMetaDataConverter;
import com.petpet.collpro.tools.SimpleGatherer;
import com.petpet.collpro.utils.Configurator;

/**
 * Hello world!
 * 
 */
public class App {
    private DigitalCollection test;
    
    public static void main(String[] args) {
        Configurator.getInstance().configure();
        App app = new App();
        app.foldertest();
        app.querytest();
    }
    
    private void foldertest() {
        this.test = new DigitalCollection("Test");
        SimpleGatherer g = new SimpleGatherer(new FITSMetaDataConverter(), test);
        g.gather(new File("/home/peter/Desktop/output/"));
        // g.gather(new File("/Users/petar/Desktop/fits/"));
    }
    
    private void querytest() {
        CollectionProfileQueries analyzer = new CollectionProfileQueries();
        System.out.println("QUERIES");
        
        List<Property> allprops = analyzer.getAllPropertiesInCollection(test);
        System.out.println("PROPS in COLLECTIOn");
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
}
