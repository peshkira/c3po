package com.petpet.c3po.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.utils.Configurator;

public class SystematicSamplingRepresentativeGenerator extends RepresentativeGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(SizeRepresentativeGenerator.class);

  private PersistenceLayer pl;

  public SystematicSamplingRepresentativeGenerator() {
    this.pl = Configurator.getDefaultConfigurator().getPersistence();
  }
  
  @Override
  public List<String> execute() {
    return this.execute(10);
  }

  @Override
  public List<String> execute(int limit) {
    LOG.info("Applying {} algorithm for representatice selection", this.getType());
    
    final List<String> result = new ArrayList<String>();
    
    long count = pl.count(Element.class, this.getFilter());
    
    if (count <= limit) {
      final Iterator<Element> cursor = this.pl.find(Element.class, this.getFilter());
      while (cursor.hasNext()) {
        result.add(cursor.next().getUid());
      }
      
    } else {
      long skip = Math.round((double) count / limit);
      LOG.debug("Calculated skip is: {}", skip);
      
      Iterator<Element> cursor = this.pl.find(Element.class, this.getFilter());
      
      while (result.size() < limit) {
        int offset = (int) ((skip * result.size() + (int) ((Math.random() * skip) % count)) % count);
        LOG.debug("offset {}", offset);
        //  skip the offset
        int i = 0;
        while (i < 0) {
          i++;
          cursor.next();
        }
        Element next = cursor.next();
        result.add(next.getUid());
        
        cursor = this.pl.find(Element.class, this.getFilter());
      }
      
    }
    
    return result;
  }

  @Override
  public String getType() {
    return "systematic sampling";
  }
  
}
