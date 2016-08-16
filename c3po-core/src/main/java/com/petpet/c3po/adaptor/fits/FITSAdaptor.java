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

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.RegexRules;
import org.apache.commons.digester3.SimpleRegexMatcher;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;

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
    private Digester digester;

    /**
     * A default constructor that initialises the digester and sets up the parsing
     * rules.
     */
    public FITSAdaptor() {
        this.digester = new Digester(); // not thread safe
        this.digester.setRules( new RegexRules( new SimpleRegexMatcher() ) );
        this.createParsingRules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        // nothing to do...
    }


    @Override
    public Element parseElement( String name, String data ) {
        Element element = null;

        if ( data == null ) {
            return element;

        }
        try {

            //DigesterContext context = new DigesterContext( this, this.getPreProcessingRules() );
            //this.digester.push( context );

            //context = (DigesterContext) this.digester.parse( new StringReader( data ) );
            //element = context.getElement();
            //List<MetadataRecord> values = context.getValues();

           // values.add(getFileExtensionMetadataRecord(name));


            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource source = new InputSource(new StringReader(data));
            Document doc = dBuilder.parse(source);

            org.w3c.dom.Element fits = (org.w3c.dom.Element) doc.getElementsByTagName("fits").item(0);

            org.w3c.dom.Element identification = (org.w3c.dom.Element) fits.getElementsByTagName("identification").item(0);
            org.w3c.dom.Element fileinfo = (org.w3c.dom.Element) fits.getElementsByTagName("fileinfo").item(0);
            org.w3c.dom.Element filestatus = (org.w3c.dom.Element) fits.getElementsByTagName("filestatus").item(0);
            org.w3c.dom.Element metadata = (org.w3c.dom.Element) fits.getElementsByTagName("metadata").item(0);

            element = createElement(fileinfo);

            readIdentification(identification, element);
            readFileinfo(fileinfo, element);
            readFileStatus(filestatus, element);
            readMetadata(metadata, element);
            element.updateStatus();


            //if ( element != null ) {
            //    element.setMetadata( values );
            //}

        } catch ( IOException e ) {
            LOG.warn( "An exception occurred while processing {}: {}", name, e.getMessage() );
        } catch ( SAXException e ) {
            LOG.warn( "An exception occurred while parsing {}: {}", name, e.getMessage() );
        } catch ( Exception e ) {
            LOG.warn( "An exception occurred while parsing {}: {}", name, e.getMessage() );
        }

        return element;
    }


   /* private MetadataRecord getFileExtensionMetadataRecord(String name) {
        MetadataRecord mr= new MetadataRecord();
        mr.setProperty(getProperty("file_extension").getKey());
        mr.setValue(FilenameUtils.getExtension(name));
        mr.setStatus("OK");
        return mr;
    }*/

    /**
     * Creates SAX based rules.
     */
    private void createParsingRules() {
        this.createElementRules();
        this.createIdentityRules();
        this.createFileInfoRules();
        this.createRepresentationInfoRules();
        this.createFileStatusRules();
        this.createMetaDataRules();
    }

    /**
     * Creates rules for the creation of the Element object.
     */
    private void createElementRules() {
        this.digester.addCallMethod( "fits", "createElement", 2 );
        this.digester.addCallParam( "fits/fileinfo/filename", 0 );
        this.digester.addCallParam( "fits/fileinfo/filepath", 1 );
    }

    /**
     * Creates rules for the parsing of the identification data within a FITS
     * file.
     */
    private void createIdentityRules() {
        this.createIdentityStatusRules();

        this.createFormatRule( "fits/identification/identity" );
        this.createFormatVersionRule( "fits/identification/identity/version" );
        this.createPuidRule( "fits/identification/identity/externalIdentifier" );

    }

    /**
     * Creates rules for parsing the identity status data within a FITS file.
     */
    private void createIdentityStatusRules() {
        this.digester.addCallMethod( "fits/identification", "setIdentityStatus", 1 );
        this.digester.addCallParam( "fits/identification", 0, "status" );
    }

    /**
     * Creates rules for parsing the format data within a FITS file.
     *
     * @param pattern
     *          the xpath to the format identity.
     */
    private void createFormatRule( String pattern ) {
        this.digester.addCallMethod( pattern, "createIdentity", 2 );
        this.digester.addCallParam( pattern, 0, "format" );
        this.digester.addCallParam( pattern, 1, "mimetype" );

        this.digester.addCallMethod( pattern + "/tool", "addIdentityTool", 2 );
        this.digester.addCallParam( pattern + "/tool", 0, "toolname" );
        this.digester.addCallParam( pattern + "/tool", 1, "toolversion" );

    }

    /**
     * Creates rules for parsing the format version data within a FITS file.
     *
     * @param pattern
     *          the xpath to the format version identity.
     */
    private void createFormatVersionRule( String pattern ) {
        this.digester.addCallMethod( pattern, "createFormatVersion", 4 );
        this.digester.addCallParam( pattern, 0 );
        this.digester.addCallParam( pattern, 1, "status" );
        this.digester.addCallParam( pattern, 2, "toolname" );
        this.digester.addCallParam( pattern, 3, "toolversion" );
    }

    /**
     * Creates rules for parsing the pronom identifier data within a FITS file.
     *
     * @param pattern
     *          the xpath to the external identifier.
     */
    private void createPuidRule( String pattern ) {
        this.digester.addCallMethod( pattern, "createPuid", 3 );
        this.digester.addCallParam( pattern, 0 );
        this.digester.addCallParam( pattern, 1, "toolname" );
        this.digester.addCallParam( pattern, 2, "toolversion" );

    }

    /**
     * Creates rules for the parsing of the file information data within a FITS
     * file.
     */
    private void createFileInfoRules() {
        this.createValueRule( "fits/fileinfo/size" );
        this.createValueRule( "fits/fileinfo/md5checksum" );
        this.createValueRule( "fits/fileinfo/lastmodified" );
        this.createValueRule( "fits/fileinfo/fslastmodified" );
        this.createValueRule( "fits/fileinfo/created" );
        this.createValueRule( "fits/fileinfo/creatingApplicationName" );
        this.createValueRule( "fits/fileinfo/creatingApplicationVersion" );
        this.createValueRule( "fits/fileinfo/inhibitorType" );
        this.createValueRule( "fits/fileinfo/inhibitorTarget" );
        this.createValueRule( "fits/fileinfo/rightsBasis" );
        this.createValueRule( "fits/fileinfo/copyrightBasis" );
        this.createValueRule( "fits/fileinfo/copyrightNote" );
        this.createValueRule( "fits/fileinfo/creatingos" );
    }

  /*
   * Experimental
   */
    /**
     * This is not part of the original FITS specification, but it is reading out
     * representation information out of RODA, if the FITS was provided by RODA.
     */
    private void createRepresentationInfoRules() {
        this.createValueRule( "fits/representationinfo/original" );
    }

    /**
     * Creates rules for parsing the file status data within a FITS file.
     */
    private void createFileStatusRules() {
        this.createValueRule( "fits/filestatus/well-formed" );
        this.createValueRule( "fits/filestatus/valid" );
        this.createValueRule( "fits/filestatus/message" );
    }

    /**
     * Creates rules for parsing the meta data section of a FITS file.
     */
    private void createMetaDataRules() {
        this.createValueRule( "fits/metadata/image/*" );
        this.createValueRule( "fits/metadata/text/*" );
        this.createValueRule( "fits/metadata/document/*" );
        this.createValueRule( "fits/metadata/audio/*" );
        this.createValueRule( "fits/metadata/video/*" );
    }

    /**
     * Creates rule for parsing generic values from FITS files.
     *
     * @param pattern
     *          the xpath to the generic meta data node.
     */
    private void createValueRule( String pattern ) {
        this.digester.addCallMethod( pattern, "createValue", 5 );
        this.digester.addCallParam( pattern, 0 );
        this.digester.addCallParam( pattern, 1, "status" );
        this.digester.addCallParam( pattern, 2, "toolname" );
        this.digester.addCallParam( pattern, 3, "toolversion" );
        this.digester.addCallParamPath( pattern, 4 );
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
            element.addMetadataRecord(property.getKey(), nodeValue, source.getId());
        }
    }


    private void readIdentification(org.w3c.dom.Element identification, Element element) {

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
                element.addMetadataRecord("format", formatValue, source.getId());
                element.addMetadataRecord("mimetype", mimetypeValue, source.getId());
            }


            NodeList externalIdentifiers = identity.getElementsByTagName("externalIdentifier");

            for (int k=0; k< externalIdentifiers.getLength();k++){
                org.w3c.dom.Element ider = (org.w3c.dom.Element) externalIdentifiers.item(k);
                String externalIdentifierToolname = ider.getAttribute("toolname");
                String externalIdentifierToolversion = ider.getAttribute("toolversion");
                Source source = getSource(externalIdentifierToolname, externalIdentifierToolversion);
                String externalIdentifierValue = ider.getTextContent();
                element.addMetadataRecord("puid",externalIdentifierValue,source.getId());
            }

            NodeList versions = identity.getElementsByTagName("version");

            for (int l=0; l< versions.getLength();l++){
                org.w3c.dom.Element version = (org.w3c.dom.Element) versions.item(l);
                String versionToolname = version.getAttribute("toolname");
                String versionToolversion = version.getAttribute("toolversion");
                Source source = getSource(versionToolname, versionToolversion);
                String versionValue = version.getTextContent();
                element.addMetadataRecord("version", versionValue, source.getId());
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
