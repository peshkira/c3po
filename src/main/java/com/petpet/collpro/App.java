package com.petpet.collpro;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.datamodel.StringValue;
import com.petpet.collpro.db.DBManager;
import com.petpet.collpro.tools.FITSMetaDataConverter;
import com.petpet.collpro.utils.Configurator;
import com.petpet.collpro.utils.XMLUtils;

/**
 * Hello world!
 * 
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        Configurator.getInstance().configure();

        File file = new File("src/main/resources/fits.xml");
        boolean valid = XMLUtils.validate(file);
        System.out.println("valid: " + valid);

        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(file);
            new FITSMetaDataConverter().extractValues(document);
            
        } catch (DocumentException e) {
            System.err.println(e.getMessage());
        }

    }
}
