package com.petpet.collpro;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.petpet.collpro.analyzer.CollectionProfileQueries;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.tools.FITSMetaDataConverter;
import com.petpet.collpro.tools.SimpleGatherer;
import com.petpet.collpro.utils.Configurator;

/**
 * Hello world!
 * 
 */
public class App {
    public static void main(String[] args) {
        Configurator.getInstance().configure();
        foldertest();
        querytest();
    }
    
    private static void foldertest() {
        SimpleGatherer g = new SimpleGatherer(new FITSMetaDataConverter(), new DigitalCollection("Test"));
        g.gather(new File("/home/peter/Desktop/output/"));
//        g.gather(new File("/Users/petar/Desktop/fits/"));
    }
    
    private static void querytest() {
        CollectionProfileQueries analyzer = new CollectionProfileQueries();
        System.out.println("QUERIES");
        
        Long count = analyzer.getElementsWithPropertyAndValueCount("mimetype", "application/pdf");
        System.out.println("PDFs: " + count);
        
        count = analyzer.getDistinctPropertyValueCount("mimetype");
        System.out.println("Distinct mimetypes: " + count);
        
        List<String> list = analyzer.getDistinctPropertyValueSet("mimetype");
        for (String v : list) {
            System.out.println("mimetype: " + v);
        }
        
        List res = analyzer.getMostOccurringProperties(5);
        System.out.println("GET MOST OCCURRING PROPERTIES");
        for (Object o : res) {
            Object[] p = (Object[]) o;
            System.out.println(Arrays.deepToString(p));
        }
        
        Long sum = analyzer.getSumOfNumericProperty("size");
        System.out.println("All elements size " + sum);
        
        Double avg = analyzer.getAverageOfNumericProperty("size");
        System.out.println("AVG elements size " + avg);
        
        res = analyzer.getValuesDistribution();
        System.out.println("DISTRIBUTION");
        for (Object o : res) {
            Object[] p = (Object[]) o;
            System.out.println(Arrays.deepToString(p));
        }
        
        res = analyzer.getSpecificPropertyValuesDistribution("format");
        System.out.println("Specific DISTRIBUTION");
        for (Object o : res) {
            Object[] p = (Object[]) o;
            System.out.println(Arrays.deepToString(p));
        }
        
    }
}
