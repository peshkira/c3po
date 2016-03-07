package com.petpet.c3po.controller;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.StreamHandler;

import static org.junit.Assert.*;

/**
 * Created by artur on 07/03/16.
 */
public class ControllerTest {
    HashMap<String, String> options = new HashMap<String, String>();
    Controller ctrl;
    @Before
    public void setUp() throws Exception {

        options.put(Constants.OPT_COLLECTION_NAME,"govdocs");
        options.put(Constants.CNF_DROOLS_PATH,"/Users/artur/rnd/git/c3po/c3po-core/src/main/resources/rules");
        Configurator.getDefaultConfigurator().configure();
        ctrl=new Controller(Configurator.getDefaultConfigurator());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testResolveConflicts() throws Exception {
        ctrl.resolveConflicts(options);


    }
}