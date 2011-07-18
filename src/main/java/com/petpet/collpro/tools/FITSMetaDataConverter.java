package com.petpet.collpro.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.common.FITSConstants;
import com.petpet.collpro.datamodel.Property;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.datamodel.ValueSource;
import com.petpet.collpro.metadata.converter.IMetaDataConverter;

public class FITSMetaDataConverter implements IMetaDataConverter {

    private static final Logger LOG = LoggerFactory.getLogger(FITSMetaDataConverter.class);

    private Date measuredAt;

    public FITSMetaDataConverter() {

    }

    @Override
    public List<Property> getProperties(String xml) {
        try {
            Document doc = DocumentHelper.parseText(xml);
            return this.getProperties(doc);

        } catch (DocumentException e) {
            LOG.error("Could not parse string, {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Property> getProperties(Document xml) {

        List<Property> props = new ArrayList<Property>();
        this.measuredAt = new Date();

        Element root = xml.getRootElement();
        Element identification = root.element(FITSConstants.IDENTIFICATION);

        Element fileinfo = root.element(FITSConstants.FILEINFO);
        Element filestatus = root.element(FITSConstants.FILESTATUS);
        // TODO check null values
        Element metadata = (Element) root.element(FITSConstants.METADATA).elements().get(0); 

        props.addAll(this.getIdentification(identification));
        props.addAll(this.getFlatProperties(fileinfo));
        props.addAll(this.getFlatProperties(filestatus));
        props.addAll(this.getFlatProperties(metadata));

        return props;
    }

    private List<Property> getIdentification(Element identification) {
        List<Property> props = new ArrayList<Property>();

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
            Property p1 = new Property();
            p1.setName(FITSConstants.FORMAT_ATTR);

            Property p2 = new Property();
            p2.setName(FITSConstants.MIMETYPE_ATTR);

            Value v1 = new Value();
            v1.setValue(format);
            v1.setMeasuredAt(this.measuredAt.getTime());
            v1.setProperty(p1);

            Value v2 = new Value();
            v2.setValue(mime);
            v2.setMeasuredAt(this.measuredAt.getTime());
            v2.setProperty(p1);

            props.add(p1);
            props.add(p2);

            System.out.println(p1.getName() + ":" + v1.getValue());
            System.out.println(p2.getName() + ":" + v2.getValue());
        }

        return props;
    }

    // TODO datatypes ...
    // TODO set reliability
    private List<Property> getFlatProperties(Element info) {
        List<Property> props = new ArrayList<Property>();

        if (info != null) {
            Iterator<Element> iter = (Iterator<Element>) info.elementIterator();
            while (iter.hasNext()) {
                Element e = iter.next();

                Property p = new Property();
                p.setName(e.getName());

                ValueSource vs = new ValueSource();
                vs.setName(e.attributeValue("toolname"));
                vs.setVersion(e.attributeValue("toolversion"));

                Value v = new Value();
                v.setValue(e.getText());
                v.setMeasuredAt(this.measuredAt.getTime());
                v.setSource(vs);
                v.setProperty(p);

                System.out.println(p.getName() + ":" + v.getValue() + " - " + vs.getName() + ":" + vs.getVersion());
            }
        }

        return props;
    }
}
