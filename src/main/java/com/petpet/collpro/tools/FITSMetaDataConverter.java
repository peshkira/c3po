package com.petpet.collpro.tools;

import java.util.Date;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.common.FITSConstants;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.datamodel.ValueSource;
import com.petpet.collpro.db.DBManager;
import com.petpet.collpro.metadata.converter.IMetaDataConverter;

public class FITSMetaDataConverter implements IMetaDataConverter {
    
    private static final Logger LOG = LoggerFactory.getLogger(FITSMetaDataConverter.class);
    
    private Date measuredAt;
    
    public FITSMetaDataConverter() {
        
    }
    
    @Override
    public void extractValues(String xml) {
        try {
            Document doc = DocumentHelper.parseText(xml);
            this.extractValues(doc);
            
        } catch (DocumentException e) {
            LOG.error("Could not parse string, {}", e.getMessage());
        }
    }
    
    @Override
    public void extractValues(Document xml) {
        this.measuredAt = new Date();
        
        Element root = xml.getRootElement();
        Element identification = root.element(FITSConstants.IDENTIFICATION);
        
        Element fileinfo = root.element(FITSConstants.FILEINFO);
        Element filestatus = root.element(FITSConstants.FILESTATUS);
        // TODO check null values
        Element metadata = (Element) root.element(FITSConstants.METADATA).elements().get(0);
        
        this.getIdentification(identification);
        this.getFlatProperties(fileinfo);
        this.getFlatProperties(filestatus);
        this.getFlatProperties(metadata);
        
    }
    
    private void getIdentification(Element identification) {
        // conflict
        if (identification.elements().size() > 1) {
            LOG.warn("There must be a conflict");
        }
        
        Iterator<Element> iter = (Iterator<Element>) identification.elementIterator();
        
        // iterate over identity tags
        while (iter.hasNext()) {
            Element identity = iter.next();
            String format = identity.attributeValue(FITSConstants.FORMAT_ATTR);
            String mime = identity.attributeValue(FITSConstants.MIMETYPE_ATTR);
            
            // TODO manage value source conflict/single_result
            Property p1 = Constants.KNOWN_PROPERTIES.get(FITSConstants.FORMAT_ATTR);
            
            if (p1 == null) {
                p1 = new Property();
                p1.setName(FITSConstants.FORMAT_ATTR);
                Constants.KNOWN_PROPERTIES.put(p1.getName(), p1);
                DBManager.getInstance().persist(p1);
            }
            
            Property p2 = Constants.KNOWN_PROPERTIES.get(FITSConstants.MIMETYPE_ATTR); 
            
            if (p2 == null) {
                p2 = new Property();
                p2.setName(FITSConstants.MIMETYPE_ATTR);
                Constants.KNOWN_PROPERTIES.put(p2.getName(), p2);
                DBManager.getInstance().persist(p2);
            }
            
            StringValue v1 = new StringValue();
            v1.setValue(format);
            v1.setMeasuredAt(this.measuredAt.getTime());
            v1.setProperty(p1);
            
            Value v2 = new StringValue();
            v2.setValue(mime);
            v2.setMeasuredAt(this.measuredAt.getTime());
            v2.setProperty(p1);
            
            DBManager.getInstance().persist(v1);
            DBManager.getInstance().persist(v2);
            
            System.out.println(p1.getName() + ":" + v1.getValue());
            System.out.println(p2.getName() + ":" + v2.getValue());
        }
    }
    
    // TODO datatypes ...
    // TODO set reliability
    private void getFlatProperties(Element info) {
        
        if (info != null) {
            Iterator<Element> iter = (Iterator<Element>) info.elementIterator();
            while (iter.hasNext()) {
                Element e = iter.next();
                
                Property p = Constants.KNOWN_PROPERTIES.get(e.getName());
                
                if (p == null) {
                    p = new Property();
                    p.setName(e.getName());
                    Constants.KNOWN_PROPERTIES.put(p.getName(), p);
                    DBManager.getInstance().persist(p);
                }
                
                ValueSource vs = new ValueSource();
                vs.setName(e.attributeValue("toolname"));
                vs.setVersion(e.attributeValue("toolversion"));
                
                Value v = new StringValue();
                v.setValue(e.getText());
                v.setMeasuredAt(this.measuredAt.getTime());
                v.setSource(vs);
                v.setProperty(p);
                
                DBManager.getInstance().persist(vs);
                DBManager.getInstance().persist(v);
                
                System.out.println(p.getName() + ":" + v.getValue() + " - " + vs.getName() + ":" + vs.getVersion());
            }
        }
        
    }
}
