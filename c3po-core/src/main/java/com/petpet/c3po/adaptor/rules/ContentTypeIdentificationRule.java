package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.ContentTypeMapping;

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
            if (mdrec.getProperty().getKey().equals("mimetype")){
                String mimetype=mdrec.getValue();
                String content_type= ContentTypeMapping.getMappingByName(mimetype);
                tmp=new MetadataRecord();
                tmp.setProperty(new Property("content_type", PropertyType.STRING));
                tmp.setValue(content_type);
                tmp.setStatus("SINGLE_RESULT");
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
        return 1;
    }

    @Override
    public void onCommandFinished() {

    }
}
