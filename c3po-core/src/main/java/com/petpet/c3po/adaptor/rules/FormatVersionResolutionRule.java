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

  @Override
  public void onCommandFinished() {
    // do nothing
  }
}
