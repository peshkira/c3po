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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester3.Digester;

import com.petpet.c3po.api.adaptor.PreProcessingRule;
import com.petpet.c3po.api.dao.ReadOnlyCache;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;

/**
 * The digester context is a simple helper object that is pushed onto the Apache
 * {@link Digester} stack and its methods are used during SAX parsing.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class DigesterContext {

  /**
   * The read only cache of C3PO to read the properties and sources.
   */
  private ReadOnlyCache cache;

  /**
   * The current element that is parsed.
   */
  private Element element;

  /**
   * A list of meta data records that are parsed.
   */
  private List<MetadataRecord> values;

  /**
   * A list of tools that reported the current format.
   */
  private List<String> formatSources;

  /**
   * A list of preprocessing rules.
   */
  private List<PreProcessingRule> rules;

  /**
   * Creates the digester context object.
   * 
   * @param cache
   *          the cache to use
   * @param rules
   *          the list of pre processing rules to apply.
   */
  public DigesterContext(ReadOnlyCache cache, List<PreProcessingRule> rules) {
    this.cache = cache;
    this.values = new ArrayList<MetadataRecord>();
    this.formatSources = new ArrayList<String>();
    this.rules = rules;
  }

  public List<MetadataRecord> getValues() {
    return values;
  }

  public Element getElement() {
    return element;
  }

  /**
   * Adds a value to the list of values.
   * 
   * @param value
   */
  public void addValue( MetadataRecord value ) {
    this.getValues().add( value );
  }

  /**
   * Creates new element and sets the element field.
   * 
   * @param name
   *          the name of the element (which is substringed)
   * @param uid
   *          the uid of the element.
   */
  public void createElement( String name, String uid ) {
    this.element = new Element( uid, this.substringPath( name ) );
  }

  /**
   * Creates a value out of the given value and information if the processing
   * rules do not skip it.
   * 
   * @param value
   *          the value to use.
   * @param status
   *          the status of the value.
   * @param toolname
   *          the toolname that reported this value.
   * @param version
   *          the version of the toolname that reported the value.
   * @param pattern
   *          the pattern (xml path) that was used
   */
  public void createValue( String value, String status, String toolname, String version, String pattern ) {
    final String propKey = this.substringPath( pattern );

    boolean shouldContinue = true;

    for ( PreProcessingRule r : rules ) {
      if ( r.shouldSkip( propKey, value, status, toolname, version ) ) {
        shouldContinue = false;
        break;
      }
    }

    if ( shouldContinue ) {
      final Property property = this.getProperty( propKey );
      final Source source = this.cache.getSource( toolname, version );

      final MetadataRecord r = new MetadataRecord();
      r.setProperty( property );
      r.setValue( value );
      r.getSources().add( source.getId() );

      if ( status != null ) {
        r.setStatus( status );
      }

      this.addValue( r );
    }
  }

  /**
   * Reads the FITS identity information and creates the format and mimetype out
   * of it.
   * 
   * @param format
   *          the format of the FITS file.
   * @param mimetype
   *          the mimetype of the FITS file.
   */
  public void createIdentity( String format, String mimetype ) {
    final Property pf = this.getProperty( "format" );
    final Property pm = this.getProperty( "mimetype" );

    this.createIdentityForProperty( pf, format );
    this.createIdentityForProperty( pm, mimetype );

    this.formatSources.clear();
  }

  /**
   * Creates an identity property (e.g. format or mimetype) out of the given
   * parameters.
   * 
   * @param property
   *          the identity property.
   * @param value
   *          the value for the given property.
   */
  private void createIdentityForProperty( Property property, String value ) {
    boolean shouldContinue = true;

    for ( PreProcessingRule r : rules ) {
      if ( r.shouldSkip( property.getId(), value, null, null, null ) ) {
        shouldContinue = false;
        break;
      }
    }

    if ( shouldContinue ) {
      MetadataRecord rec = new MetadataRecord();
      rec.setProperty( property );
      rec.setValue( value );
      rec.getSources().addAll( this.formatSources );
      this.addValue( rec );
    }
  }

  /**
   * Adds the given tool information to the format sources list.
   * 
   * @param toolname
   *          the name of the tool
   * @param version
   *          the version of the tool.
   */
  public void addIdentityTool( String toolname, String version ) {
    final Source s = this.cache.getSource( toolname, version );
    this.formatSources.add( s.getId() );
  }

  /**
   * Sets the status of the identity to the given status, if it is not null.
   * 
   * @param status
   *          the status to set.F
   */
  public void setIdentityStatus( String status ) {
    if ( status != null ) {
      if ( status.equals( MetadataRecord.Status.SINGLE_RESULT.name() ) ) {
        this.updateStatusOf( "puid", Status.CONFLICT.name() );
      }

      this.updateStatusOf( "format", status );
      this.updateStatusOf( "mimetype", status );

    }
  }

  /**
   * Creates the format version property.
   * 
   * @param value
   *          the value to use.
   * @param status
   *          the status of the value for this property.
   * @param toolname
   *          the tool that reported it.
   * @param version
   *          the version of the tool that reported it.
   */
  public void createFormatVersion( String value, String status, String toolname, String version ) {
    final Property pf = this.getProperty( "format_version" );
    final Source s = this.cache.getSource( toolname, version );
    final MetadataRecord fmtv = new MetadataRecord();

    fmtv.setProperty( pf );
    fmtv.setValue( value );
    fmtv.getSources().add( s.getId() );

    if ( status != null ) {
      fmtv.setStatus( status );
    }

    this.addValue( fmtv );
  }

  /**
   * Creates a pronom identifier record.
   * 
   * @param value
   *          the puid value.
   * @param toolname
   *          the name of the tool.
   * @param version
   *          the version of the tool.
   */
  public void createPuid( String value, String toolname, String version ) {
    final Property pp = this.getProperty( "puid" );
    final Source s = this.cache.getSource( toolname, version );
    final MetadataRecord puid = new MetadataRecord();

    puid.setProperty( pp );
    puid.setValue( value );
    puid.getSources().add( s.getId() );

    this.addValue( puid );
  }

  /**
   * Substrings the given string by the last index of a '/' or '\' and returns
   * the new string (or the same if there were no delimeters).
   * 
   * @param str
   *          the string to chop.
   * @return the new substring.
   */
  private String substringPath( String str ) {
    if ( str != null ) {
      str = str.substring( str.lastIndexOf( "/" ) + 1 );
      str = str.substring( str.lastIndexOf( "\\" ) + 1 );
    }
    return str;
  }

  /**
   * Updates the status of all properties with the given name to the passed
   * status.
   * 
   * @param pName
   *          the name of the property.
   * @param status
   *          the status to set.
   */
  private void updateStatusOf( String pName, String status ) {
    Property property = this.getProperty( pName );
    for ( MetadataRecord v : this.values ) {
      if ( v.getProperty().getId().equals( property.getId() ) ) {
        v.setStatus( status );
      }
    }
  }

  /**
   * Gets the property object out of the cache for the given name. It maps the
   * FITS property to a C3PO property.
   * 
   * @param name
   *          the name of the FITS property.
   * @return the {@link Property} object.
   */
  private Property getProperty( String name ) {
    final String prop = FITSHelper.getPropertyKeyByFitsName( name );
    return this.cache.getProperty( prop );
  }

}
