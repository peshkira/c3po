package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
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
            if (mdrec.getProperty().equals("mimetype") && !mdrec.getStatus().equals("CONFLICT")){
                String mimetype=mdrec.getValues().get(0);
                String content_type= ContentTypeMapping.getMappingByName(mimetype);
                if (content_type!=null) {
                    tmp = new MetadataRecord();
                    tmp.setProperty(new Property("content_type", PropertyType.STRING).getKey());
                    tmp.getValues().add(content_type);
                    tmp.setStatus("SINGLE_RESULT");
                    tmp.getSources().add(new Source("C3PO", "0.6").getId());
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
