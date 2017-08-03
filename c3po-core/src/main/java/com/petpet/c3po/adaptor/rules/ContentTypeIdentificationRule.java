package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.ContentTypeMapping;

import java.util.Collection;
import java.util.List;

/**
 * Created by artur on 08.09.15.
 */
public class ContentTypeIdentificationRule implements PostProcessingRule {
    @Override
    public Element process(Element e) {
        List<MetadataRecord> metadataRecords = e.getMetadata();
        MetadataRecord tmp=null;
        for (MetadataRecord mdrec: metadataRecords){
            if (mdrec.getProperty().equals("mimetype")) {
                Collection<String> values = mdrec.getSourcedValues().values();
                tmp = new MetadataRecord();
                tmp.setProperty(new Property("content_type", PropertyType.STRING).getKey());
                tmp.setStatus(mdrec.getStatus());
                Source c3po = Configurator.getDefaultConfigurator().getPersistence().getCache().getSource("C3PO", "0.6");
                for (String value : values) {
                    String content_type= ContentTypeMapping.getMappingByName(value);
                    tmp.getSourcedValues().put(c3po.getId(),content_type);
                }
                break;
            }
        }
        if (tmp!=null){
            metadataRecords.add(tmp);
        }
        return e;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void onCommandFinished() {

    }
}
