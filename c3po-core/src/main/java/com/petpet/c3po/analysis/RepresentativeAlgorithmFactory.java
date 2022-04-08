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

import com.petpet.c3po.utils.Configurator;

/**
 * A simple factory that chooses the representative sample selection algorithm
 * based on the specified id. In order to use the user specified algorithm take
 * a look at the {@link Configurator#getStringProperty(String)} method and use
 * the 'c3po.samples.algorithm' property
 * 
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class RepresentativeAlgorithmFactory {

  /**
   * The known algorithms.
   */
  private static final String[] ALGORITHMS = { "sizesampling", "syssampling", "distsampling", "sfd" };

  /**
   * The factory method.
   * 
   * @param id
   *          the id of the algorithm implementation
   * @return the implementation of the algorithm.
   */
  public RepresentativeGenerator getAlgorithm( String id ) {
    RepresentativeGenerator gen = null;

    int pos = 0;
    for ( int i = 0; i < ALGORITHMS.length; i++ ) {
      if ( ALGORITHMS[i].equalsIgnoreCase( id ) ) {
        pos = i;
        break;
      }
    }

    switch ( pos ) {
      case 0:
        gen = new SizeRepresentativeGenerator();
        break;
      case 1:
        gen = new SystematicSamplingRepresentativeGenerator();
        break;
      case 2:
        gen = new DistributionRepresentativeGenerator();
        break;
      case 3:
        gen= new SelectiveFeatureDistributionSampling();
        break;
      default:
        gen = new SizeRepresentativeGenerator();
    }

    return gen;
  }

  /**
   * Checks if the given algorithm is supported and returns true if yes, false
   * otherwise.
   * 
   * @param alg
   *          the algorithm name to check.
   * @return true if supported, false otherwise.
   */
  public static boolean isValidAlgorithm( String alg ) {
    if ( alg == null ) {
      return false;
    }

    for ( String a : ALGORITHMS ) {
      if ( a.equalsIgnoreCase( alg ) ) {
        return true;
      }
    }

    return false;
  }
}
