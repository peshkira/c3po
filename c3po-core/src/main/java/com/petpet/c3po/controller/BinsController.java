package com.petpet.c3po.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by artur on 31/08/16.
 *
 * This class decides what bins are necessary for a given property. The data is read from from an external config file.
 */
public class BinsController {
    static Map<String, List<Integer>> binsPerProperty;
    public static List<Integer> getBins(String property){
        if (binsPerProperty==null){
            binsPerProperty=loadBinsConfig();

        }
        return null;
    }

    private static Map<String, List<Integer>> loadBinsConfig() {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "vocabulary.properties" );
            InputStreamReader reader=new InputStreamReader(in);
            //reader.
           // TERMS = new Properties();
            //TERMS.load( in );
            in.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }


}
