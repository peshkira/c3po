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
package com.petpet.c3po.analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.BetweenFilterCondition;
import com.petpet.c3po.api.model.helper.BetweenFilterCondition.Operator;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.api.model.helper.NumericStatistics;
import com.petpet.c3po.utils.Configurator;

/**
 * The size representative generator is a strategy for selecting samples based
 * on size. It selects the largest, smallest and a few average-sized elements.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class SizeRepresentativeGenerator extends RepresentativeGenerator {

  /**
   * Default logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger( SizeRepresentativeGenerator.class );

  /**
   * The persistence layer.
   */
  private PersistenceLayer pl;

  /**
   * Creates the generator.
   */
  public SizeRepresentativeGenerator() {
    this.pl = Configurator.getDefaultConfigurator().getPersistence();
  }

  /**
   * Selects 10 samples per default.
   */
  @Override
  public List<String> execute() {
    return execute( 10 );
  }

  @Override
  public List<String> execute( int limit ) {
    LOG.info( "Applying {} algorithm for representatice selection", this.getType() );
    final Set<String> result = new HashSet<String>();
    long count = pl.count( Element.class, this.getFilter() );
    if ( count <= limit ) {
      Iterator<Element> iter = this.pl.find( Element.class, this.getFilter() );
      while ( iter.hasNext() ) {
        result.add( iter.next().getUid() );
      }
    } else {
      NumericStatistics statistics = this.pl.getNumericStatistics( pl.getCache().getProperty( "size" ), this
          .getFilter() );

      double min = statistics.getMin();
      double max = statistics.getMax();
      double avg = statistics.getAverage();
      double sd = statistics.getStandardDeviation();

      double low = Math.floor( (avg - sd / 10) );
      double high = Math.ceil( (avg + sd / 10) );

      Filter minFilter = new Filter( this.getFilter() );
      minFilter.addFilterCondition( new FilterCondition( "size", min ) );

      Filter maxFilter = new Filter( this.getFilter() );
      maxFilter.addFilterCondition( new FilterCondition( "size", max ) );

      Filter avgFilter = new Filter( this.getFilter() );
      avgFilter.addFilterCondition( new BetweenFilterCondition( "size", Operator.LTE, high, Operator.GTE, low ) );

      Iterator<Element> minCursor = this.pl.find( Element.class, minFilter );
      Iterator<Element> maxCursor = this.pl.find( Element.class, maxFilter );
      Iterator<Element> avgCursor = this.pl.find( Element.class, avgFilter );

      if ( minCursor.hasNext() && result.size() < limit ) {
        result.add( minCursor.next().getId() );
      }

      if ( maxCursor.hasNext() && result.size() < limit ) {
        result.add( maxCursor.next().getId() );
      }

      while ( avgCursor.hasNext() && result.size() < limit ) {
        result.add( avgCursor.next().getId() );
      }

    }
    return Arrays.asList( result.toArray( new String[0] ) );
  }

  public String getType() {
    return "size'o'matic 3000";
  }
}
