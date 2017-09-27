package com.petpet.c3po.analysis;

import com.petpet.c3po.adaptor.fits.FITSAdaptor;
import com.petpet.c3po.adaptor.fits.FITSHelper;
import com.petpet.c3po.adaptor.rules.AssignCollectionToElementRule;
import com.petpet.c3po.adaptor.rules.CreateElementIdentifierRule;
import com.petpet.c3po.adaptor.rules.EmptyValueProcessingRule;
import com.petpet.c3po.analysis.conflictResolution.Rule;
import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.adaptor.ProcessingRule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.filtering.PropertyFilterCondition;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.dao.MongoPersistenceLayerTest;
import com.petpet.c3po.dao.mongo.MongoPersistenceLayer;
import com.petpet.c3po.gatherer.LocalFileGatherer;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import com.petpet.c3po.utils.XMLUtils;
import com.petpet.c3po.utils.exceptions.C3POPersistenceException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by artur on 31/03/16.
 */
public class ConflictResolutionProcessorTest {
    PersistenceLayer pLayer;
    final Logger LOG = LoggerFactory.getLogger(MongoPersistenceLayerTest.class);

    @Before
    public void setUp() throws Exception {
        System.out.println("Setting up the test. Inserting the data");

        helpers.DataOps.insertData();
        pLayer = Configurator.getDefaultConfigurator().getPersistence();

    }


    @After
    public void tearDown() throws Exception {
        System.out.println("Tearing down the test. Removing the data");
        helpers.DataOps.removeData();
    }

    @Test
    public void resolve() throws Exception {
        ConflictResolutionProcessor crp=new ConflictResolutionProcessor();
        Rule rule=new Rule();
        Element element=new Element(null,null);
        Property creating_application_version = pLayer.getCache().getProperty("creating_application_version");
        MetadataRecord mr=new MetadataRecord(creating_application_version.getKey(),"10 (107b)");
        element.getMetadata().add(mr);

        Filter filter=new Filter();

        List<PropertyFilterCondition> pfcs=new ArrayList<PropertyFilterCondition>();

        filter.setPropertyFilterConditions(pfcs);

        PropertyFilterCondition pfc1=new PropertyFilterCondition();

        pfc1.setProperty("creating_application_version");
        List<String> statuses1=new ArrayList<String>();
        statuses1.add("CONFLICT");
        pfc1.setStatuses(statuses1);
        pfcs.add(pfc1);

        PropertyFilterCondition pfc2=new PropertyFilterCondition();
        pfc2.setProperty("format");
        List<String> values2=new ArrayList<String>();
        values2.add("Microsoft Word");
        pfc2.setValues(values2);
        pfcs.add(pfc2);

        rule.setFilter(filter);
       // rule.setElement(element);
        List<Rule> rules=new ArrayList<Rule>();
        rules.add(rule);
        crp.setRules(rules);
       // long conflictsCount = crp.getConflictsCount(filter);

        //Map<String, Integer> test = crp.getOverview("test", filter);

//        long resolve = crp.resolve(null);
      //  Assert.assertEquals(resolve, 1);
    }



}