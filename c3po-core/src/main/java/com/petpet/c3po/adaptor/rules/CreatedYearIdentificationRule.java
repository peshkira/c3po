package com.petpet.c3po.adaptor.rules;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import com.petpet.c3po.utils.Configurator;
import org.apache.commons.io.FilenameUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by artur on 08.09.15.
 */
public class CreatedYearIdentificationRule implements PostProcessingRule {
    @Override
    public Element process(Element e) {
        List<MetadataRecord> metadata = e.getMetadata();
        for (MetadataRecord metadataRecord : metadata) {
            if (metadataRecord.getProperty().equals("created")){
                MetadataRecord tmp = new MetadataRecord();
                Property created_yearP = Configurator.getDefaultConfigurator().getPersistence().getCache().getProperty("created_year", PropertyType.STRING);
                tmp.setProperty(created_yearP.getKey());
                tmp.setStatus(metadataRecord.getStatus());
                Source c3po = Configurator.getDefaultConfigurator().getPersistence().getCache().getSource("C3PO", "0.6");

                for (String s : metadataRecord.getSourcedValues().values()) {
                    if (s.length()>4) {
                        String substring = s.substring(0, 4);   //TODO: Find a better way to extract year
                        tmp.getSourcedValues().put(c3po.getId(),substring);
                    }
                }
                e.getMetadata().add(tmp);
                break;
            }
        }
        return e;
    }

    @Override
    public int getPriority() {
        return 101;
    }

    @Override
    public void onCommandFinished() {

    }
}
