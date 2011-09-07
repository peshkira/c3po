package com.petpet.collpro.metadata.converter;

import com.petpet.collpro.utils.ConfigurationException;
import com.petpet.collpro.utils.Observable;

import java.util.Map;

public interface IMetaDataConverter extends Observable {

    /**
     * Extracts the values from the elements setup by the configuration. All
     * created elements are passed via the notifyObservers method back to the
     * observer for further processing.
     * 
     */
    void convert();

    /**
     * Sets the configuration parameters of the gatherer. For instance the
     * Collecton to on which the gatherer will work, the file path, eventually
     * server config, etc.
     * 
     * @param configuration
     *            the configuration map.
     */
    void configure(Map<String, Object> configuration) throws ConfigurationException;

}
