package com.petpet.collpro.metadata.converter;

import org.dom4j.Document;

import com.petpet.collpro.datamodel.Element;

public interface IMetaDataConverter {
    
    /**
     * Returns an element representing the file from which the fits xml file was
     * created. It contains all its values, that were extracted by the parser.
     * 
     * @param xml
     *            the fits xml as a string representation.
     * @return an element object with all values.
     */
    Element extractValues(String xml);
    
    /**
     * Returns an element representing the file from which the fits xml file was
     * created. It contains all its values, that were extracted by the parser.
     * 
     * @param xml
     *            the fits xml as a xml {@link Document} object representation.
     * @return an element object with all values.
     */
    Element extractValues(Document xml);
    
}
