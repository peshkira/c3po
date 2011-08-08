package com.petpet.collpro.metadata.converter;

import org.dom4j.Document;

public interface IMetaDataConverter {

    void extractValues(String xml);
    
    void extractValues(Document xml);
    
}
