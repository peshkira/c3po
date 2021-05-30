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
package com.petpet.c3po.adaptor.rules;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.adaptor.PreProcessingRule;

/**
 * A {@link PreProcessingRule} that cleans up values provided by a special tool
 * bundled in FITS by TU Wien. Note that this rule is turned off by default and
 * can be enabled via the .c3poconfig file with the following key set to true:
 * 'c3po.rule.html_info_processing'.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class HtmlInfoProcessingRule implements PreProcessingRule {

  /**
   * A default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( HtmlInfoProcessingRule.class );

  /**
   * A set of valid html tags.
   */
  private Set<String> tags;

  public HtmlInfoProcessingRule() {
    this.tags = new HashSet<String>();
    this.readTags();
  }

  /**
   * Reads the valid html tags into memory.
   */
  private void readTags() {
    try {
      BufferedReader reader = new BufferedReader( new InputStreamReader( HtmlInfoProcessingRule.class.getClassLoader()
          .getResourceAsStream( "adaptors/htmltags" ) ) );
      String line = reader.readLine();
      while ( line != null ) {

        this.tags.add( line );

        line = reader.readLine();
      }
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    } catch ( IOException e ) {
      e.printStackTrace();
    }

  }

  /**
   * Has the lowest possible priority.
   */
  @Override
  public int getPriority() {
    return 1;
  }

  /**
   * Skips the value if the property is not a valid html tag.
   */
  @Override
  public boolean shouldSkip( String property, String value, String status, String tool, String version ) {

    if ( tool != null && tool.equalsIgnoreCase( "HtmlInfo" ) ) {

      if ( property.endsWith( "rences" ) ) {
        int tagIndex = property.indexOf( "Tag" );

        if ( tagIndex == -1 ) {
          return true;
        }

        String tag = property.substring( 0, tagIndex );

        if ( !this.tags.contains( tag ) ) {
          LOG.debug( "Property {} seems to be faulty, skip", property );
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public void onCommandFinished() {
    // do nothing
  }
}
