package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.Configurator;
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
        tmp.setProperty(new Property("file_extension", PropertyType.STRING).getKey());
        tmp.getValues().add(file_extension);
        tmp.setStatus("SINGLE_RESULT");
        Source c3PO = Configurator.getDefaultConfigurator().getPersistence().getCache().getSource("C3PO", "0.6");
        tmp.getSources().add(c3PO.toString());
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
