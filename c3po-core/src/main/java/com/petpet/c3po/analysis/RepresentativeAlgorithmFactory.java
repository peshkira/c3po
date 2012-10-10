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
  private static final String[] ALGORITHMS = { "size", "systematic sampling" };

  /**
   * The factory method.
   * 
   * @param id
   *          the id of the algorithm implementation
   * @return the implementation of the algorithm.
   */
  public RepresentativeGenerator getAlgorithm(String id) {
    RepresentativeGenerator gen = null;

    int pos = 0;
    for (int i = 0; i < ALGORITHMS.length; i++) {
      if (ALGORITHMS[i].equalsIgnoreCase(id)) {
        pos = i;
        break;
      }
    }

    switch (pos) {
      case 0:
        gen = new SizeRepresentativeGenerator();
        break;
      case 1:
        gen = new SystematicSamplingRepresentativeGenerator();
        break;
    }

    return gen;
  }
}
