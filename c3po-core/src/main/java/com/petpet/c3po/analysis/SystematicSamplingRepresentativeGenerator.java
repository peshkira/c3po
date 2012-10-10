package com.petpet.c3po.analysis;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.DataHelper;

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
    final BasicDBObject query = DataHelper.getFilterQuery(this.getFilter());
    
    LOG.debug("Query: " + query.toString());
    
    long count = pl.count(Constants.TBL_ELEMENTS, query);
    
    if (count <= limit) {
      final DBCursor cursor = this.pl.find(Constants.TBL_ELEMENTS, query);
      while (cursor.hasNext()) {
        result.add(DataHelper.parseElement(cursor.next(), this.pl).getUid());
      }
      
    } else {
      long skip = Math.round((double) count / limit);
      LOG.debug("Calculated skip is: {}", skip);
      
      DBCursor cursor = this.pl.find(Constants.TBL_ELEMENTS, query);
      
      while (result.size() < limit) {
        int offset = (int) ((skip * result.size() + (int) ((Math.random() * skip) % count)) % count);
        LOG.debug("offset {}", offset);
        DBObject next = cursor.skip(offset).next();
        result.add(DataHelper.parseElement(next, this.pl).getUid());
        
        cursor = this.pl.find(Constants.TBL_ELEMENTS, query);
      }
      
    }
    
    return result;
  }

  @Override
  public String getType() {
    return "systematic sampling";
  }
  
}
