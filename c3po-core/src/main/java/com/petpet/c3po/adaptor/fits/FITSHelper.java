package com.petpet.c3po.adaptor.fits;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.petpet.c3po.datamodel.Property;
import com.petpet.c3po.utils.Helper;

public class FITSHelper {

    private static Properties FITS_PROPS;

    public static void init() {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("fits_property_mapping.properties");
            FITS_PROPS = new Properties();
            FITS_PROPS.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Property getPropertyByFitsName(String name) {
        String prop = (String) FITS_PROPS.get(name);

        Property p;
        if (prop != null) {
            p = Helper.getPropertyByName(prop);
        } else {
            p = Helper.getPropertyByName(name);
        }

        return p;
    }
}
