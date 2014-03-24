package com.petpet.c3po.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by artur on 3/24/14.
 */
public class VocabularyDP {
    private static Properties TERMS;


    public static void init() {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "vocabulary.properties" );
            TERMS = new Properties();
            TERMS.load( in );
            in.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the mapping for the given fits property name or the same name if no
     * mapping was defined.
     *
     * @param name
     *          the fits property name to look for.
     * @return the mapping or the fits name.
     */
    public static String getUriByName(String name) {
        final String prop = (String) TERMS.get( name );
        return (prop == null) ? null : prop;
    }

}
