package com.petpet.c3po.adaptor.rules;

import java.util.List;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;

/**
 * A post processing rule to correct a FITS reported conflict in the format
 * version that was marked as ok. This rule is turned on per default and can be
 * disabled via the .c3poconfig file with the following key set to false:
 * 'c3po.rule.format_version_resolution'.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class FormatVersionResolutionRule implements PostProcessingRule {

  /**
   * Has a high priotity.
   */
  @Override
  public int getPriority() {
    return 900;
  }

  /**
   * Make sure that all format_version have the correct status. Especially when
   * there is a CONFLICT
   */
  @Override
  public Element process( Element e ) {
    List<MetadataRecord> formatVersionRecords = e.removeMetadata( "format_version" );
    if ( formatVersionRecords.size() > 1 ) {
      for ( MetadataRecord mr : formatVersionRecords ) {
        mr.setStatus( Status.CONFLICT.name() );
      }
    }

    e.getMetadata().addAll( formatVersionRecords );

    return e;
  }

}
