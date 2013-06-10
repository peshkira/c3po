/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.petpet.c3po.adaptor.browsershot;

import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author artur
 */
public class BrowserShotParser {

  public static List<MetadataRecord> parse(String data) {
    Document dom;
    try {
      DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      dom = dBuilder.parse(IOUtils.toInputStream(data));
      dom.normalize();

      List<MetadataRecord> records = new ArrayList<MetadataRecord>();
      NodeList nodes = dom.getElementsByTagName("comparisonResult");

      for (int i = 0; i < nodes.getLength(); i++) {
        records.addAll(processNode(nodes.item(i)));
      }
      return records;
    } catch (Exception e) {
      System.out.print(e);
      return null;
    }
    
  }

  private static List<MetadataRecord> processNode(Node n) {
    List<MetadataRecord> records = new ArrayList<MetadataRecord>();
    if ("comparisonResult".equals(n.getNodeName())) {
      String nodename = n.getAttributes().getNamedItem("baseurl").getTextContent();
      Property p = new Property("baseurl", PropertyType.STRING);
      MetadataRecord record = new MetadataRecord(p, nodename);
      records.add(record);
      NodeList nodes = n.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        records.addAll(processNode(nodes.item(i)));
      }
    } else if ("browser_desc".equals(n.getNodeName())) {
      String nodevalue = n.getTextContent();
      Property p = new Property("browser_desc", PropertyType.STRING);
      MetadataRecord record = new MetadataRecord(p, nodevalue);
      records.add(record);
    } else if ("browser_id".equals(n.getNodeName())) {
      String nodevalue = n.getTextContent();
      Property p = new Property("browser_id", PropertyType.STRING);
      MetadataRecord record = new MetadataRecord(p, nodevalue);
      records.add(record);
    } else if ("score".equals(n.getNodeName())) {
      String nodevalue = n.getTextContent();
      Property p = new Property("score", PropertyType.FLOAT);
      MetadataRecord record = new MetadataRecord(p, nodevalue);
      records.add(record);
    } else if ("confronter".equals(n.getNodeName())) {
      NodeList nodes = n.getChildNodes();
      List<MetadataRecord> tmprecords = new ArrayList<MetadataRecord>();
      for (int i = 0; i < nodes.getLength(); i++) {
        tmprecords.addAll(processNode(nodes.item(i)));
      }
      Property p = new Property(tmprecords.get(0).getValue(), PropertyType.FLOAT);
      MetadataRecord record = new MetadataRecord(p, tmprecords.get(2).getValue());
      records.add(record);
      
      
      
    }
    return records;
  }
}
