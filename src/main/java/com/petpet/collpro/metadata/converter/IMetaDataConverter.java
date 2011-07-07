package com.petpet.collpro.metadata.converter;

import java.util.List;

import org.dom4j.Document;

import com.petpet.collpro.datamodel.Property;

public interface IMetaDataConverter {

    List<Property> getProperties(String xml);
    
    List<Property> getProperties(Document xml);
    
}
