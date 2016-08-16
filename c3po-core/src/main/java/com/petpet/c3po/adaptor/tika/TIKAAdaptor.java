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
package com.petpet.c3po.adaptor.tika;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.adaptor.AbstractAdaptor;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.Source;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.PropertyType;

/**
 * A C3PO adaptor for RAW Apache TIKA output.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class TIKAAdaptor extends AbstractAdaptor {

  private static final Logger LOG = LoggerFactory.getLogger( TIKAAdaptor.class );

  private static final String OPT_TIKA_VERSION = "c3po.adaptor.tika.version";

  private String version = "";

  /**
   * {@inheritDoc}
   */
  @Override
  public void configure() {
    this.version = this.getStringConfig( OPT_TIKA_VERSION, "" );
  }

  /**
   * Parses the TIKA data.
   */
  @Override
  public Element parseElement( String name, String data ) {
    try {
      Map<String, String> metadata = TIKAResultParser.getKeyValueMap( data );
      Element element = this.createElement( metadata );
      return element;
    } catch ( IOException e ) {
      LOG.warn( "Could not parse data for {}", name );
      return null;
    }
  }

  @Override
  public String getAdaptorPrefix() {
    return "tika";
  }

  /**
   * Creates an element out of the parsed metadata map.
   * 
   * @param metadata
   *          the map of property value pairs.
   * @return the element object.
   */
  private Element createElement( Map<String, String> metadata ) {

    String name = metadata.remove( "resourceName" );
    if ( name == null ) {
      return null;
    }

    Element element = new Element( name, name );
    List<MetadataRecord> records = new ArrayList<MetadataRecord>();

    for ( String key : metadata.keySet() ) {
      String value = metadata.get( key );
      key = key.replace( '.', '_' ).replace( ' ', '_' ).replace( ':', '_' ).toLowerCase();
      key = TIKAHelper.getPropertyKeyByTikaName( key );

      if ( key != null ) {
        Property prop = this.getProperty( key );
        if ( prop.getType().equals( PropertyType.INTEGER.name() ) || prop.getType().equals( PropertyType.FLOAT.name() ) ) {
          value = value.split( " " )[0];
        }
        MetadataRecord record = new MetadataRecord( prop.getKey(), value );
        Source source = this.getSource( "Tika", this.version );
        record.setSources( Arrays.asList( source.getId() ) );
        records.add( record );
      }
    }

    element.setMetadata( records );
    return element;

  }

}
