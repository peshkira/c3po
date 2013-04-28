package com.petpet.c3po.adaptor.rules;

import java.util.List;

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.MetadataRecord;
import com.petpet.c3po.api.model.helper.MetadataRecord.Status;

/**
 * A simple post processing rule to correct a FITS reported conflict in
 * the format version that was marked as ok.
 * @author Petar Petrov <me@petarpetrov.org>
 *
 */
public class FormatVersionResolutionRule implements PostProcessingRule {

  @Override
  public int getPriority() {
    return 900;
  }

  /**
   * Make sure that all format_version have the correct status.
   * Especially when there is a CONFLICT
   */
  @Override
  public Element process(Element e) {
    List<MetadataRecord> formatVersionRecords = e.removeMetadata("format_version");
    if (formatVersionRecords.size() > 1) {
      for (MetadataRecord mr : formatVersionRecords) {
        mr.setStatus(Status.CONFLICT.name());
      }
    }
    
    e.getMetadata().addAll(formatVersionRecords);
    
    return e;
  }

}
