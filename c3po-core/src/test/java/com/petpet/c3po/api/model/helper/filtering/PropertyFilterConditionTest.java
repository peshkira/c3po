package com.petpet.c3po.api.model.helper.filtering;

import com.petpet.c3po.api.model.helper.MetadataRecord;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition.PropertyFilterConditionType.*;

/**
 * Created by artur on 09/12/2016.
 */
public class PropertyFilterConditionTest {
    @Test
    public void addCondition() throws Exception {

        PropertyFilterCondition fpc = new PropertyFilterCondition();


        fpc.addCondition(PROPERTY, "format");
        fpc.addCondition(PROPERTY, "mimetype");
        fpc.addCondition(STATUS, MetadataRecord.Status.CONFLICT);
        Map<String, String> hm = new HashMap<String, String>();
        hm.put("jhove", "text/html");
        hm.put("droid", "application/xml");
        fpc.addCondition(SOURCEDVALUE, hm);

    }

    @Test
    public void deleteCondition() throws Exception {

    }

}