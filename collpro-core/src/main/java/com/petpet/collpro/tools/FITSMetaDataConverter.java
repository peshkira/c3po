package com.petpet.collpro.tools;

import com.petpet.collpro.api.adapter.IMetaDataConverter;
import com.petpet.collpro.api.utils.ConfigurationException;
import com.petpet.collpro.common.FITSConstants;
import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.datamodel.ValueSource;
import com.petpet.collpro.datamodel.ValueStatus;
import com.petpet.collpro.utils.Helper;
import com.petpet.collpro.utils.XMLUtils;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FITSMetaDataConverter implements IMetaDataConverter {

    private static final Logger LOG = LoggerFactory.getLogger(FITSMetaDataConverter.class);

    private Map<String, Object> configuration;

    private Date measuredAt;

    private DigitalCollection collection;

    private Set<ChangeListener> observers;

    private File[] files;

    private SAXReader reader;

    public FITSMetaDataConverter() {
        this.observers = new HashSet<ChangeListener>();
        this.reader = new SAXReader();
    }

    @Override
    public void convert() {
        for (File f : files) {
            this.extractMetaData(f);
        }
    }

    @Override
    public void configure(Map<String, Object> configuration) throws ConfigurationException {
        this.configuration = configuration;

        Object c = this.configuration.get("config.collection");

        if (c == null) {
            throw new ConfigurationException(
                    "No collection provided, please set a collection for field 'config.collection'");
        } else {
            this.collection = (DigitalCollection) c;
        }

        Object f = this.configuration.get("config.fits_files");
        if (f == null) {
            throw new ConfigurationException(
                    "No file array provided, please set a file array for field 'config.fits_files'");
        } else {
            this.files = (File[]) f;
        }

        Object d = this.configuration.get("config.date");
        if (d == null) {
            this.measuredAt = new Date();

        } else {
            Date date = (Date) d;
            this.measuredAt = date;
        }
    }

    private Element extractValues(Document xml) {
        org.dom4j.Element root = xml.getRootElement();
        org.dom4j.Element fileinfo = root.element(FITSConstants.FILEINFO);
        org.dom4j.Element identification = root.element(FITSConstants.IDENTIFICATION);
        org.dom4j.Element filestatus = root.element(FITSConstants.FILESTATUS);
        org.dom4j.Element metadata = (org.dom4j.Element) root.element(FITSConstants.METADATA);
        if (metadata != null && metadata.elements().size() > 0) {
            // fetch first tag (one of document, audio, video, image, etc...)
            metadata = (org.dom4j.Element) metadata.elements().get(0);
        }

        String md5 = fileinfo.element(FITSConstants.MD5CHECKSUM).getText();
        String filename = fileinfo.element(FITSConstants.FILENAME).getText();
        String filepath = fileinfo.element(FITSConstants.FILEPATH).getText();

        boolean processed = Helper.isElementAlreadyProcessed(this.collection, md5);
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

    private void extractMetaData(File f) {
        if (f.isFile()) {
            try {
                Document document = reader.read(f);
                Element element = this.extractValues(document);
                element.setCollection(this.collection);
                notifyObservers(element);

            } catch (DocumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void getIdentification(org.dom4j.Element identification, Element e) {
        ValueStatus stat = ValueStatus.OK;

        if (identification.elements().size() > 1) {
            LOG.warn("There are more than one identity tags. There must be a conflict");
            stat = ValueStatus.valueOf(identification.attributeValue("status"));
        }

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
            vs.setName(identity.element(FITSConstants.TOOL).attributeValue(FITSConstants.TOOL_ATTR));
            vs.setVersion(identity.element(FITSConstants.TOOL).attributeValue(FITSConstants.TOOLVERSION_ATTR));

            StringValue v1 = new StringValue(format);
            v1.setMeasuredAt(this.measuredAt.getTime());
            v1.setProperty(p1);
            v1.setElement(e);
            v1.setSource(vs);
            v1.setStatus(stat);

            StringValue v2 = new StringValue(mime);
            v2.setMeasuredAt(this.measuredAt.getTime());
            v2.setProperty(p2);
            v2.setElement(e);
            v2.setSource(vs);
            v2.setStatus(stat);

            e.getValues().add(v1);
            e.getValues().add(v2);

            // System.out.println(p1.getName() + ":" + v1.getTypedValue());
            // System.out.println(p2.getName() + ":" + v2.getTypedValue());

            Property p3 = Helper.getPropertyByName(FITSConstants.FORMAT_VERSION_ATTR);

            Iterator verIter = identity.elementIterator(FITSConstants.VERSION);
            while (verIter.hasNext()) {
                org.dom4j.Element ver = (org.dom4j.Element) verIter.next();

                vs = new ValueSource(ver.attributeValue(FITSConstants.TOOL_ATTR),
                        ver.attributeValue(FITSConstants.TOOLVERSION_ATTR));

                StringValue v = new StringValue(ver.getText());
                v.setMeasuredAt(this.measuredAt.getTime());
                v.setProperty(p3);
                v.setElement(e);
                v.setSource(vs);

                if (stat.equals(ValueStatus.OK))
                    v.setStatus(XMLUtils.getStatusOfFITSElement(ver));
                else
                    v.setStatus(stat);

                e.getValues().add(v);

                // System.out.println(p3.getName() + ":" + v.getTypedValue());
            }

            Iterator extIterator = identity.elementIterator(FITSConstants.EXT_ID);
            while (extIterator.hasNext()) {
                org.dom4j.Element extId = (org.dom4j.Element) extIterator.next();
                Property p = Helper.getPropertyByName(extId.attributeValue(FITSConstants.EXT_ID_TYPE_ATTR));

                vs = new ValueSource(extId.attributeValue(FITSConstants.TOOL_ATTR),
                        extId.attributeValue(FITSConstants.TOOLVERSION_ATTR));

                StringValue v = new StringValue(extId.getText());
                v.setMeasuredAt(this.measuredAt.getTime());
                v.setProperty(p);
                v.setElement(e);
                v.setSource(vs);

                if (stat.equals(ValueStatus.OK))
                    v.setStatus(XMLUtils.getStatusOfFITSElement(extId));
                else
                    v.setStatus(stat);

                e.getValues().add(v);

                // System.out.println(p.getName() + ":" + v.getTypedValue());
            }
        }
    }

    // TODO set reliability
    private void getFlatProperties(org.dom4j.Element info, Element e) {

        if (info != null) {
            Iterator<org.dom4j.Element> iter = (Iterator<org.dom4j.Element>) info.elementIterator();
            while (iter.hasNext()) {
                org.dom4j.Element elmnt = iter.next();

                Property p = Helper.getPropertyByName(elmnt.getName());

                ValueSource vs = new ValueSource(elmnt.attributeValue(FITSConstants.TOOL_ATTR),
                        elmnt.attributeValue(FITSConstants.TOOLVERSION_ATTR));

                // System.out.println("Value of property: " + p.getName() + " "
                // + p.getType());
                Value v = Helper.getTypedValue(p.getType(), elmnt.getText());
                v.setMeasuredAt(this.measuredAt.getTime());
                v.setSource(vs);
                v.setProperty(p);
                v.setElement(e);
                v.setStatus(XMLUtils.getStatusOfFITSElement(elmnt));

                e.getValues().add(v);

                // System.out.println(p.getName() + ":" + v.getTypedValue() +
                // " - " + vs.getName() + ":" + vs.getVersion());
            }
        }
    }

    @Override
    public void addObserver(ChangeListener listener) {
        if (!this.observers.contains(listener)) {
            this.observers.add(listener);
        }
    }

    @Override
    public void removeObserver(ChangeListener listener) {
        this.observers.remove(listener);

    }

    @Override
    public void notifyObservers(Object source) {
        for (ChangeListener l : this.observers) {
            final ChangeEvent evt = new ChangeEvent(source);
            l.stateChanged(evt);
        }
    }

}
