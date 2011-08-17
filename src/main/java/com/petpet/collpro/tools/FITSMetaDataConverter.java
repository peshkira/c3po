package com.petpet.collpro.tools;

import java.util.Date;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.FITSConstants;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.datamodel.ValueSource;
import com.petpet.collpro.metadata.converter.IMetaDataConverter;
import com.petpet.collpro.utils.Helper;
import com.petpet.collpro.utils.XMLUtils;

public class FITSMetaDataConverter implements IMetaDataConverter {
    
    private static final Logger LOG = LoggerFactory.getLogger(FITSMetaDataConverter.class);
    
    private Date measuredAt;
    
    public FITSMetaDataConverter() {
        
    }
    
    @Override
    public Element extractValues(String xml) {
        try {
            Document doc = DocumentHelper.parseText(xml);
            return this.extractValues(doc);
            
        } catch (DocumentException e) {
            LOG.error("Could not parse string, {}", e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public Element extractValues(Document xml) {
        this.measuredAt = new Date();
        
        org.dom4j.Element root = xml.getRootElement();
        org.dom4j.Element fileinfo = root.element(FITSConstants.FILEINFO);
        org.dom4j.Element identification = root.element(FITSConstants.IDENTIFICATION);
        org.dom4j.Element filestatus = root.element(FITSConstants.FILESTATUS);
        org.dom4j.Element metadata = (org.dom4j.Element) root.element(FITSConstants.METADATA);
        if (metadata != null) {
            // fetch first tag (one of document, audio, video, image, etc...)
            metadata = (org.dom4j.Element) metadata.elements().get(0);
        }
        
        String md5 = fileinfo.element(FITSConstants.MD5CHECKSUM).getText();
        String filename = fileinfo.element(FITSConstants.FILENAME).getText();
        String filepath = fileinfo.element(FITSConstants.FILEPATH).getText();
        
        boolean processed = Helper.isElementAlreadyProcessed(md5);
        if (processed) {
            LOG.info("Element '{}' is already processed", filename);
            return null;
        }
        
        Element e = new Element(filename, filepath);
        
        this.getIdentification(identification, e);
        this.getFlatProperties(fileinfo, e);
        this.getFlatProperties(filestatus, e);
        this.getFlatProperties(metadata, e);
        
        return e;
    }
    
    private void getIdentification(org.dom4j.Element identification, Element e) {
        // TODO handle conflict
        if (identification.elements().size() > 1) {
            LOG.warn("There are more than one identity tags. There must be a conflict");
        }
        
        // TODO check for version conflict in identity tag
        Iterator<org.dom4j.Element> iter = (Iterator<org.dom4j.Element>) identification.elementIterator();
        
        // iterate over identity tags
        while (iter.hasNext()) {
            org.dom4j.Element identity = iter.next();
            String format = identity.attributeValue(FITSConstants.FORMAT_ATTR);
            String mime = identity.attributeValue(FITSConstants.MIMETYPE_ATTR);
            
            // TODO manage value source conflict/single_result
            Property p1 = Helper.getPropertyByName(FITSConstants.FORMAT_ATTR);
            Property p2 = Helper.getPropertyByName(FITSConstants.MIMETYPE_ATTR);
            
            ValueSource vs = new ValueSource();
            vs.setName(identity.element("tool").attributeValue("toolname"));
            vs.setVersion(identity.element("tool").attributeValue("toolversion"));
            
            // TODO handle conflicts
            StringValue v1 = new StringValue();
            v1.setValue(format);
            v1.setMeasuredAt(this.measuredAt.getTime());
            v1.setProperty(p1);
            v1.setElement(e);
            v1.setSource(vs);
            
            StringValue v2 = new StringValue();
            v2.setValue(mime);
            v2.setMeasuredAt(this.measuredAt.getTime());
            v2.setProperty(p2);
            v2.setElement(e);
            v2.setSource(vs);
            
            e.getValues().add(v1);
            e.getValues().add(v2);

            System.out.println(p1.getName() + ":" + v1.getValue());
            System.out.println(p2.getName() + ":" + v2.getValue());
            
            Property p3 = Helper.getPropertyByName(FITSConstants.FORMAT_VERSION_ATTR);
            
            Iterator verIter = identity.elementIterator("version");
            while (verIter.hasNext()) {
                org.dom4j.Element version = (org.dom4j.Element) verIter.next();
                
                vs = new ValueSource();
                vs.setName(version.attributeValue("toolname"));
                vs.setVersion(version.attributeValue("toolversion"));
                
                StringValue v = new StringValue(version.getText());
                v.setMeasuredAt(this.measuredAt.getTime());
                v.setProperty(p3);
                v.setElement(e);
                v.setSource(vs);
                v.setStatus(XMLUtils.getStatusOfFITSElement(version));
                
                e.getValues().add(v);
                
                System.out.println(p3.getName() + ":" + v.getValue());
            }
            
            // TODO handle external identifiers.
            
        }
    }
    
    // TODO set reliability
    private void getFlatProperties(org.dom4j.Element info, Element e) {
        
        if (info != null) {
            Iterator<org.dom4j.Element> iter = (Iterator<org.dom4j.Element>) info.elementIterator();
            while (iter.hasNext()) {
                org.dom4j.Element elmnt = iter.next();
                
                Property p = Helper.getPropertyByName(elmnt.getName());
                
                ValueSource vs = new ValueSource();
                vs.setName(elmnt.attributeValue("toolname"));
                vs.setVersion(elmnt.attributeValue("toolversion"));
                
                System.out.println("Value of property: " + p.getName() + " " + p.getType());
                Value v = Helper.getTypedValue(p.getType(), elmnt.getText());
                v.setMeasuredAt(this.measuredAt.getTime());
                v.setSource(vs);
                v.setProperty(p);
                v.setElement(e);
                v.setStatus(XMLUtils.getStatusOfFITSElement(elmnt));
                
                e.getValues().add(v);
                
                System.out.println(p.getName() + ":" + v.getValue() + " - " + vs.getName() + ":" + vs.getVersion());
            }
        }
    }
    
}
