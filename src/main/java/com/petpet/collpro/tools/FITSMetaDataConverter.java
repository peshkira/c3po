package com.petpet.collpro.tools;

import java.util.Date;
import java.util.Iterator;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.Constants;
import com.petpet.collpro.common.FITSConstants;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.PropertyType;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.datamodel.ValueSource;
import com.petpet.collpro.db.DBManager;
import com.petpet.collpro.metadata.converter.IMetaDataConverter;
import com.petpet.collpro.utils.Helper;

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
        Element fileinfo = root.element(FITSConstants.FILEINFO);
        Element identification = root.element(FITSConstants.IDENTIFICATION);
        Element filestatus = root.element(FITSConstants.FILESTATUS);
        Element metadata = (Element) root.element(FITSConstants.METADATA).elements().get(0);
        // TODO check null values in metadata
        
        String md5 = fileinfo.element(FITSConstants.MD5CHECKSUM).getText();
        String filename = fileinfo.element(FITSConstants.FILENAME).getText();
        String filepath = fileinfo.element(FITSConstants.FILEPATH).getText();
        
        boolean processed = this.isAlreadyProcessed(md5);
        if (processed) {
            LOG.info("Element '{}' is already processed", filename);
            return;
        }

        com.petpet.collpro.datamodel.Element e = new com.petpet.collpro.datamodel.Element();
        e.setName(filename);
        e.setPath(filepath);
        
        DBManager.getInstance().persist(e);
        
        this.getIdentification(identification, e);
        this.getFlatProperties(fileinfo, e);
        this.getFlatProperties(filestatus, e);
        this.getFlatProperties(metadata, e);
        
    }
    
    private boolean isAlreadyProcessed(String md5) {
        if (md5 == null || md5.equals("")) {
            LOG.warn("No checksum provided, assuming element is not processed.");
            return false;
        }
        
        boolean isDone = false;
        LOG.debug("MD5: {}", md5);
        
        try {
            DBManager.getInstance().getEntityManager().createNamedQuery("getMD5ChecksumValue", StringValue.class)
                .setParameter("hash", md5).getSingleResult();
            isDone = true;
        } catch (NoResultException nre) {
            LOG.debug("No element with this checksum ingested, continue processing.");
            isDone = false;
            
        } catch (NonUniqueResultException nue) {
            LOG.warn("More than one elements with this checksum are already processed. Please inspect");
            isDone = true;
        }
        
        return isDone;
    }
    
    private void getIdentification(Element identification, com.petpet.collpro.datamodel.Element e) {
        // TODO handle conflict
        if (identification.elements().size() > 1) {
            LOG.warn("There must be a conflict");
        }
        
        // TODO check for version conflict in identity tag
        
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
                p1.setType(PropertyType.STRING);
                Constants.KNOWN_PROPERTIES.put(p1.getName(), p1);
                DBManager.getInstance().persist(p1);
            }
            
            Property p2 = Constants.KNOWN_PROPERTIES.get(FITSConstants.MIMETYPE_ATTR);
            
            if (p2 == null) {
                p2 = new Property();
                p2.setName(FITSConstants.MIMETYPE_ATTR);
                p2.setType(PropertyType.STRING);
                Constants.KNOWN_PROPERTIES.put(p2.getName(), p2);
                DBManager.getInstance().persist(p2);
            }
            
            StringValue v1 = new StringValue();
            v1.setValue(format);
            v1.setMeasuredAt(this.measuredAt.getTime());
            v1.setProperty(p1);
            v1.setElement(e);
            
            Value v2 = new StringValue();
            v2.setValue(mime);
            v2.setMeasuredAt(this.measuredAt.getTime());
            v2.setProperty(p2);
            v2.setElement(e);
            
            DBManager.getInstance().persist(v1);
            DBManager.getInstance().persist(v2);
            
            e.getValues().add(v1);
            e.getValues().add(v2);
            DBManager.getInstance().getEntityManager().merge(e);
            
            System.out.println(p1.getName() + ":" + v1.getValue());
            System.out.println(p2.getName() + ":" + v2.getValue());
        }
    }
    
    // TODO set reliability
    // TODO handle conflicts
    private void getFlatProperties(Element info, com.petpet.collpro.datamodel.Element e) {
        
        if (info != null) {
            Iterator<Element> iter = (Iterator<Element>) info.elementIterator();
            while (iter.hasNext()) {
                Element elmnt = iter.next();
                
                Property p = Constants.KNOWN_PROPERTIES.get(elmnt.getName());
                
                if (p == null) {
                    p = new Property();
                    p.setName(elmnt.getName());
                    p.setType(Helper.getType(p.getName()));
                    Constants.KNOWN_PROPERTIES.put(p.getName(), p);
                    DBManager.getInstance().persist(p);
                }
                
                ValueSource vs = new ValueSource();
                vs.setName(elmnt.attributeValue("toolname"));
                vs.setVersion(elmnt.attributeValue("toolversion"));
                
                System.out.println("Value of property: " + p.getName() + " " + p.getType());
                Value v = Helper.getTypedValue(p.getType(), elmnt.getText());
                v.setMeasuredAt(this.measuredAt.getTime());
                v.setSource(vs);
                v.setProperty(p);
                v.setElement(e);
                
                DBManager.getInstance().persist(vs);
                DBManager.getInstance().persist(v);
                
                e.getValues().add(v);
                
                System.out.println(p.getName() + ":" + v.getValue() + " - " + vs.getName() + ":" + vs.getVersion());
            }
            
            DBManager.getInstance().getEntityManager().merge(e);
        }
        
    }
}
