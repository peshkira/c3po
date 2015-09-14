package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by artur on 08.09.15.
 */
public class ContentTypeMapping {
    static Properties TERMS;
    public static void init() {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "content_type_mapping.properties" );
            TERMS = new Properties();
            TERMS.load( in );
            in.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
    public static String getMappingByName(String name) {
        if (TERMS==null)
            init();
        final String prop = (String) TERMS.get( name );
        return (prop == null) ? "Unknown" : prop;
    }
}

