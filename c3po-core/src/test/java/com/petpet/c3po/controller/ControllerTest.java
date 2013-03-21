/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.petpet.c3po.controller;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;
import com.petpet.c3po.utils.TestConfigGenerator;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author artur
 */
public class ControllerTest {
    
    public ControllerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    private TestConfigGenerator helper;
    @Before
    public void setUp() {
        
    helper = new TestConfigGenerator();
    helper.backupRealUserConfigFile();
    helper.copyTestConfigFile();
    
    final Configurator configurator = Configurator.getDefaultConfigurator();
    configurator.configure();
    this.pLayer = configurator.getPersistence();
    }
    
    @After
    public void tearDown() {
        helper.restoreUserConfigFile();
        pLayer.close();
    }
     private PersistenceLayer pLayer;

    /**
     * Test of collect method, of class Controller.
     */
    @Test
    public void testCollect()  {
        System.out.println("collect");
  
        //arrange
        Map<String, Object> config  = new HashMap<String, Object>();
        Filter filter  = new Filter("test", null, null);
        Controller instance = new Controller(pLayer);
        String pathToFITS="src/test/resources/fits/";  //"/home/artur/Data/test/";
        
        config.put(Constants.CNF_COLLECTION_NAME, "test");
        config.put(Constants.CNF_COLLECTION_LOCATION, pathToFITS);
        config.put(Constants.CNF_RECURSIVE, false);
        config.put(Constants.CNF_EXTRACT, false);
        config.put(Constants.CNF_THREAD_COUNT, 8);
        config.put(Constants.CNF_INFER_DATE, false);
        
        //act
        instance.collect(config);
        List<String> filteredcollection = DataHelper.getFilteredElements(filter);
        int collectionsize= filteredcollection.size();
        int filesamount=new File(pathToFITS).listFiles().length;
        
        //assert
       assertEquals(collectionsize, filesamount);
        
    }

}