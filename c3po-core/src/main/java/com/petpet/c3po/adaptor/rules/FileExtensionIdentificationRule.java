package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import org.apache.commons.io.FilenameUtils;

import java.util.List;

/**
 * Created by artur on 08.09.15.
 */
public class FileExtensionIdentificationRule implements PostProcessingRule {
    @Override
    public Element process(Element e) {

        String file_extension= FilenameUtils.getExtension(e.getUid());
        MetadataRecord tmp=new MetadataRecord();
        tmp.setProperty(new Property("file_extension", PropertyType.STRING));
        tmp.setValue(file_extension);
        tmp.setStatus("SINGLE_RESULT");
        e.getMetadata().add(tmp);
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
