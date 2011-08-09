package com.petpet.collpro.tools;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
        boolean processed = this.isAlreadyProcessed(fileinfo.element(FITSConstants.MD5CHECKSUM));
        if (processed) {
            LOG.info("Element {} is already processed", ""); // TODO set element
                                                             // name
            return;
        }
        
        Element identification = root.element(FITSConstants.IDENTIFICATION);
        Element filestatus = root.element(FITSConstants.FILESTATUS);
        // TODO check null values
        Element metadata = (Element) root.element(FITSConstants.METADATA).elements().get(0);
        
        this.getIdentification(identification);
        this.getFlatProperties(fileinfo);
        this.getFlatProperties(filestatus);
        this.getFlatProperties(metadata);
        
    }
    
    private boolean isAlreadyProcessed(Element md5checksum) {
        if (md5checksum == null) {
            LOG.warn("No checksum provided, assuming element is not processed.");
            return false;
        }
        
        boolean isDone = false;
        String md5 = md5checksum.getText();
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
    
    private void getIdentification(Element identification) {
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
                p1.setType(PropertyType.STRING);
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
    
    // TODO set reliability
    // TODO handle conflicts
    private void getFlatProperties(Element info) {
        
        if (info != null) {
            Iterator<Element> iter = (Iterator<Element>) info.elementIterator();
            while (iter.hasNext()) {
                Element e = iter.next();
                
                Property p = Constants.KNOWN_PROPERTIES.get(e.getName());
                
                if (p == null) {
                    p = new Property();
                    p.setName(e.getName());
                    p.setType(Helper.getType(p.getName()));
                    Constants.KNOWN_PROPERTIES.put(p.getName(), p);
                    DBManager.getInstance().persist(p);
                }
                
                ValueSource vs = new ValueSource();
                vs.setName(e.attributeValue("toolname"));
                vs.setVersion(e.attributeValue("toolversion"));
                
                System.out.println("Value of property: " + p.getName() + " " + p.getType());
                Value v = Helper.getTypedValue(p.getType(), e.getText());
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
