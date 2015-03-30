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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.utils.Configurator;

/**
 * This class generates a sample set based on a distribution coverage strategy.
 * It calculates the distributions of the values of each of the given properties
 * and tries to find a sample set that has similar distribution for the given
 * values.
 * 
 * <b>Note</b> that giving many properties will most certainly result in an
 * empty set. <b>Note</b> that giving exclusive properties (e.g. pagecount and
 * audio_dataencoding) will always result in an empty set. <b>Note</b> that this
 * version of the strategy does not detect outliers.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class DistributionRepresentativeGenerator extends RepresentativeGenerator {

  /**
   * Executes with the default sample size of 10. Returns the list of
   * identifiers with at most 10 elements.
   */
  @Override
  public List<String> execute() {
    return this.execute( 10 );
  }

  /**
   * Executes with the given sample size and returns a list of size up to the
   * given limit.
   */
  @Override
  public List<String> execute( int limit ) {
    final List<String> properties = this.getProperties();
    final List<String> result = new ArrayList<String>();

    if ( properties.isEmpty() ) {
      throw new IllegalArgumentException( "No properties were provided for distribution calculation" );
    }

    // for each property
    // find all distinct property value pairs
    // e.g. for valid and well-formed:
    // valid=yes, well-formed=yes; valid=yes, well-formed=no; valid=no,
    // well-formed=no; valid=no, well-formed=yes;
    // valid=unknown, well-formed=yes; valid=unknown, well-formed=no; valid=yes,
    // well-formed=unknown; valid=no, well-formed=unknown;
    // valid=unknown, well-formed=unknown

    // for each distinct property value pair (combination) find the file
    // occurrences and sort the property value pair
    // (combinations) descending.

    // start from the most occurring one and calculate its percentage of the
    // overall count (consider the filter query)

    // based on the percentage, calculate the absolute value (round up) and
    // query 'n' random elements from the filter set
    // that have this property-value pair (combination).

    // proceed with the last 2 steps until the limit is reached.

    PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
    long overallCount = pl.count( Element.class, this.getFilter() );
    FilterCondition[][] matrix = new FilterCondition[properties.size()][];
    for ( int i = 0; i < properties.size(); i++ ) {
      String key = properties.get( i );
      List<String> distinct = pl.distinct( Element.class, properties.get( i ), this.getFilter() );
      FilterCondition[] values = new FilterCondition[distinct.size()];
      for ( int j = 0; j < distinct.size(); j++ ) {
        values[j] = new FilterCondition( key, distinct.get( j ) );
      }

      matrix[i] = values;

    }

    // System.out.println(Arrays.deepToString(matrix));

    List<Combination> combinations = new ArrayList<Combination>();
    Set<List<FilterCondition>> results = this.combinations( matrix );
    for ( List<FilterCondition> combs : results ) {
      Filter query = new Filter( this.getFilter() );
      for ( FilterCondition c : combs ) {
        query.addFilterCondition( c );
      }

      long count = pl.count( Element.class, query );
      combinations.add( new Combination( query, count ) );
    }

    Collections.sort( combinations, new CombinationComparator() );

    for ( Combination c : combinations ) {
      if ( c.count > 0 && result.size() < limit ) {

          Iterator<Element> cursor = pl.find( Element.class, c.query );
          if (cursor.hasNext())
              result.add( cursor.next().getUid() );
        //double percent = c.count * 100 / overallCount;
        //int tmpLimit = (int) Math.round( percent / 100 * limit );

        //Iterator<Element> cursor = pl.find( Element.class, c.query );
        // System.out.println(c.query + " count: " + c.count + " percent: " +
        // percent + "% absolute: " + tmpLimit);
        //while ( cursor.hasNext() && tmpLimit != 0 && result.size() < limit ) {
        //  result.add( cursor.next().getUid() );
        //  tmpLimit--;
        //}
      }

    }

    return result;
  }

  /*
   * some crazy shit.
   */
  /**
   * Builds all combinations of the given filter conditions.
   * 
   * @param opts
   *          the conditions.
   * @return returns a set of lists of filter conditions.
   */
  private Set<List<FilterCondition>> combinations( FilterCondition[][] opts ) {

    Set<List<FilterCondition>> results = new HashSet<List<FilterCondition>>();

    if ( opts.length == 1 ) {
      for ( FilterCondition s : opts[0] )
        results.add( new ArrayList<FilterCondition>( Arrays.asList( s ) ) );
    } else
      for ( FilterCondition obj : opts[0] ) {
        FilterCondition[][] tail = Arrays.copyOfRange( opts, 1, opts.length );
        for ( List<FilterCondition> combs : combinations( tail ) ) {
          combs.add( obj );
          results.add( combs );
        }
      }
    return results;
  }

  /**
   * A combination has a filter query and count of objects matching the query.
   * 
   * @author Petar Petrov <me@petarpetrov.org>
   * 
   */
  private static class Combination {

    private Filter query;

    private long count;

    public Combination(Filter query, long count) {
      this.query = query;
      this.count = count;
    }

  }

  /**
   * Sorts the combinations descending according to the combination count.
   * 
   * @author Petar Petrov <me@petarpetrov.org>
   * 
   */
  private class CombinationComparator implements Comparator<Combination> {
    @Override
    public int compare( Combination c1, Combination c2 ) {
      return new Long( c2.count ).compareTo( c1.count ); // descending
    }

  }

  @Override
  public String getType() {
    return "distribution sampling";
  }

  /**
   * Obtains the properties, for which this generator will build the
   * distributions.
   * 
   * @return the list of properties or an empty list.
   */
  private List<String> getProperties() {
    final Map<String, Object> options = this.getOptions();
    List<String> properties = (List<String>) options.get( "properties" );

    if ( properties == null ) {
      properties = new ArrayList<String>();
    }

    return properties;
  }

}
