package com.petpet.collpro.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.collpro.datamodel.DigitalCollection;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.datamodel.RepresentativeCollection;
import com.petpet.collpro.datamodel.Value;
import com.petpet.collpro.db.PreparedQueries;

public class CollectionInspector {

  private static final Logger LOG = LoggerFactory.getLogger(CollectionInspector.class);
  
  private PreparedQueries queries;

  public RepresentativeCollection getRepresentativeCollection(DigitalCollection coll, int limit, String... properties) {
    List<Element> result = new ArrayList<Element>();

    if (coll == null) {
      LOG.warn("No collection was provided, returning empty representative collection");
      return new RepresentativeCollection();
    }
    
    if (properties == null) {
      LOG.warn("No properties provided, returning empty representative collection");
      // consider using the null value as a representative collection
      // over all elements. (thin of a heuristic for that... as it
      // might take a while)
      
      return new RepresentativeCollection();
    }
    
    if (limit >= coll.getElements().size()) {
      LOG.warn("There are less than {} elements in the collection '{}', returning all elements", limit, coll.getName());
      return new RepresentativeCollection(coll, coll.getElements());
    }
    
    int collSize = coll.getElements().size();
    LOG.debug("Size of collection is {}", collSize);
    
    Set<Element> elements = new HashSet<Element>();
    for (String pname : properties) {
      LOG.debug("calculating distributions for property {}", pname);
      List<Object[]> distr; 
         if (elements.isEmpty()) {
           distr = this.queries.getSpecificPropertyValuesDistribution(pname, coll);
         } else {
           distr = this.queries.getSpecificPropertyValuesDistributionInSet(pname, new ArrayList<Element>(elements));
         }
      
      for (Object[] o : distr) {
        String val = (String) o[1];
        long occ = (Long) o[2];
        double percent = Math.round(((double) occ) * 100 / collSize);
        int els = (int) Math.round(percent / 100 * limit);
        
        LOG.debug("Value '{}' has {} occurrences in the collection", val, occ);
        LOG.debug("calculated percent is {}", percent);
        LOG.debug("calculated occurrence count in representative collection is {}", els);
        
        List<Value> values = this.queries.getValueByPropertyNameAndValue(pname, val, coll);
        
        if (values.size() < els) {
          LOG.warn("Something must have gone wrong during the calculations");
        }
        
        for (Value v : values) {
          if (els > 0) {
            elements.add(v.getElement());
            els--;
          } else {
            break;
          }
        }
      } 
    }

    return new RepresentativeCollection(coll, elements);
  }

  public PreparedQueries getQueries() {
    return queries;
  }

  public void setQueries(PreparedQueries queries) {
    this.queries = queries;
  }
}
