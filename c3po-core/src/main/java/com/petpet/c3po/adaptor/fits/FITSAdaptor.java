/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.adaptor.fits;


import java.io.StringReader;
import java.util.*;

import com.mongodb.QueryBuilder;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * An adaptor for FITS <url>https://github.com/harvard-lts/fits</url> meta data.
 * It makes use of the Apache Commons Digester to parse the files.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 *
 */
public class FITSAdaptor extends AbstractAdaptor {

    /**
     * A default logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger( FITSAdaptor.class );

    /**
     * The apache digester to process the fits xml meta data.
     */


    /**
     * A default constructor that initialises the digester and sets up the parsing
     * rules.
     */
    public FITSAdaptor() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        // nothing to do...
    }

    /**
     * Parses the meta data and retrieves it.
     */
    @Override
    public Element parseElement( String name, String data ) {

        if ( data == null ) {
            return new Element();
        }
        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource source = new InputSource(new StringReader(data));
            Document doc = dBuilder.parse(source);

            org.w3c.dom.Element fits = (org.w3c.dom.Element) doc.getElementsByTagName("fits").item(0);

            org.w3c.dom.Element identification = (org.w3c.dom.Element) fits.getElementsByTagName("identification").item(0);
            org.w3c.dom.Element fileinfo = (org.w3c.dom.Element) fits.getElementsByTagName("fileinfo").item(0);
            org.w3c.dom.Element filestatus = (org.w3c.dom.Element) fits.getElementsByTagName("filestatus").item(0);
            org.w3c.dom.Element metadata = (org.w3c.dom.Element) fits.getElementsByTagName("metadata").item(0);

            Element element = createElement(fileinfo);


            readIdentification(identification, element);
            readFileinfo(fileinfo, element);
            readFileStatus(filestatus, element);
            readMetadata(metadata, element);
            //updateStatus(element);
            return element;
        }  catch ( Exception e ) {
            LOG.warn( "An exception occurred while parsing {}: {}", name, e.getMessage() );
            return new Element();
        }
    }

    private void updateStatus(Element element) {
        if (element.getMetadata()==null)
            return;
        for (MetadataRecord mr : element.getMetadata()) {
            int sourceCount = distinctCount(mr.getSourcedValues().keySet());
            int propValCount = distinctCount(mr.getSourcedValues().values());
            if (propValCount > 1)
                mr.setStatus(MetadataRecord.Status.CONFLICT.name());
            else {
                if (sourceCount > 1)
                    mr.setStatus(MetadataRecord.Status.OK.name());
                else
                    mr.setStatus(MetadataRecord.Status.SINGLE_RESULT.name());
            }
        }
    }

    private int distinctCount(Collection<String> strings) {
        List<String> unique=new ArrayList<String>();
        for (String string : strings) {
            if (!unique.contains(string))
                unique.add(string);
        }
        return unique.size();
    }

    private void readMetadata(org.w3c.dom.Element metadata, Element element) {
        NodeList childNodes = metadata.getChildNodes();
        for (int i=0;i<childNodes.getLength();i++){
            NodeList childChildNodes = childNodes.item(i).getChildNodes();
            for (int j=0;j<childChildNodes.getLength();j++) {
                Node item = childChildNodes.item(j);
                if (item!= null && item.getNodeType() == Node.ELEMENT_NODE)
                    readGenericMetadata(item, element);
            }
        }
    }

    private void readFileStatus(org.w3c.dom.Element filestatus, Element element) {
        NodeList childNodes = filestatus.getChildNodes();
        for (int i=0;i<childNodes.getLength();i++){
            readGenericMetadata(childNodes.item(i), element);
        }
    }

    private void readFileinfo(org.w3c.dom.Element fileinfo, Element element) {
        NodeList childNodes = fileinfo.getChildNodes();
        for (int i=0;i<childNodes.getLength();i++){
            readGenericMetadata(childNodes.item(i), element);
        }
    }

    private void readGenericMetadata(Node item, Element element) {
        if (item.getNodeType() == org.w3c.dom.Node.TEXT_NODE){
            //ignore "/n" values!
        } else if (item.getNodeType() == Node.ELEMENT_NODE) {
            org.w3c.dom.Element elementDOM = (org.w3c.dom.Element) item;
            String propertyName = elementDOM.getNodeName();
            String toolname = elementDOM.getAttribute("toolname");
            String toolversion = elementDOM.getAttribute("toolversion");
            String nodeValue = elementDOM.getTextContent();
            Source source = getSource(toolname, toolversion);
            Property property = getProperty(propertyName);
            String status = elementDOM.getAttribute("status");
           // if (property.getType().equals(PropertyType.DATE))

            element.addMetadataRecord(property.getKey(), nodeValue, source.toString(), status);
        }
    }


    private void readIdentification(org.w3c.dom.Element identification, Element element) {
        String status = identification.getAttribute("status");
        if (status==null || status.equals(""))
            status="OK";
        NodeList identities = identification.getElementsByTagName("identity");
        for (int i=0; i < identities.getLength();i++){
            org.w3c.dom.Element identity = (org.w3c.dom.Element) identities.item(i);
            String formatValue = identity.getAttribute("format");
            String mimetypeValue = identity.getAttribute("mimetype");
            NodeList tools = identity.getElementsByTagName("tool");
            for (int j=0;j<tools.getLength();j++){
                org.w3c.dom.Element tool = (org.w3c.dom.Element) tools.item(j);
                String toolname = tool.getAttribute("toolname");
                String toolversion = tool.getAttribute("toolversion");
                Source source = getSource(toolname, toolversion);
                Property format = getProperty("format");
                Property mimetype = getProperty("mimetype");
                element.addMetadataRecord(format.getKey(), formatValue, source.toString(), status);
                element.addMetadataRecord(mimetype.getKey(), mimetypeValue, source.toString(), status);
            }


            NodeList externalIdentifiers = identity.getElementsByTagName("externalIdentifier");

            for (int k=0; k< externalIdentifiers.getLength();k++){
                org.w3c.dom.Element ider = (org.w3c.dom.Element) externalIdentifiers.item(k);
                String externalIdentifierToolname = ider.getAttribute("toolname");
                String externalIdentifierToolversion = ider.getAttribute("toolversion");
                Source source = getSource(externalIdentifierToolname, externalIdentifierToolversion);
                String externalIdentifierValue = ider.getTextContent();
                Property puid = getProperty("puid");
                element.addMetadataRecord(puid.getKey(),externalIdentifierValue,source.toString(),status);
            }

            NodeList versions = identity.getElementsByTagName("version");

            for (int l=0; l< versions.getLength();l++){
                org.w3c.dom.Element version = (org.w3c.dom.Element) versions.item(l);
                String versionToolname = version.getAttribute("toolname");
                String versionToolversion = version.getAttribute("toolversion");
                Source source = getSource(versionToolname, versionToolversion);
                String versionValue = version.getTextContent();
                Property formatversion = getProperty("formatversion");
                element.addMetadataRecord(formatversion.getKey(), versionValue, source.toString(), status);
            }
        }
    }

    private Element createElement(org.w3c.dom.Element fileinfo) {
        String filename = fileinfo.getElementsByTagName("filename").item(0).getTextContent();
        String filepath = fileinfo.getElementsByTagName("filepath").item(0).getTextContent();
        return new Element(filename,this.substringPath(filepath));
    }

    public Element createElementTemplate(String name, String uid ) {
        Element result =new Element(uid, this.substringPath( name ) );
        return result;
    }

    private String substringPath( String str ) {
        if ( str != null ) {
            str = str.substring( str.lastIndexOf( "/" ) + 1 );
            str = str.substring( str.lastIndexOf( "\\" ) + 1 );
        }
        return str;
    }

    /**
     * Returns the prefix of this adaptor ('fits').
     */
    @Override
    public String getAdaptorPrefix() {
        return "fits";
    }

    /**
     * {@inheritDoc}
     */
    public Property getProperty( String key ) {
        String prop = FITSHelper.getPropertyKeyByFitsName( key );
        return super.getProperty( prop );
    }

    /**
     * {@inheritDoc}
     */
    public Source getSource( String name, String version ) {
        return super.getSource( name, version );
    }





}
