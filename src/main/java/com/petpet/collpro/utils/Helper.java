package com.petpet.collpro.utils;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.datamodel.Property;

public final class Helper {

    /*
     * not very efficient as knwon properties will grow...
     * think of another strategy for later on..
     */
    public static Property getProperty(String name) {
        for (Property p : Constants.KNOWN_PROPERTIES) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }
    
    private Helper() {
        
    }
}
