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

import com.petpet.c3po.api.adaptor.PostProcessingRule;
import com.petpet.c3po.api.model.Element;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A post processing rule to correct a FITS reported conflict in the format
 * version that was marked as ok. This rule is turned on per default and can be
 * disabled via the .c3poconfig file with the following key set to false:
 * 'c3po.rule.format_version_resolution'.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 */
public class IdenticalValueConflictResolutionRule implements PostProcessingRule {

    /**
     * Has a high priotity.
     */
    @Override
    public int getPriority() {
        return 900;
    }

    @Override
    public Element process(Element e) {
        e.getMetadata().parallelStream().forEach((record) -> {
            List<String> values = record.getValues();
            if (values.size() == 1) {
                record.setStatus("SINGLE_RESULT");
            } else if (values.size() > 1) {
                List<String> distinctValues = values.stream().distinct().collect(Collectors.toList());
                if (distinctValues.size() > 1) {
                    record.setStatus("CONFLICT");
                } else {
                    record.setStatus("OK");
                }
            }
        });

        return e;
    }

    @Override
    public void onCommandFinished() {
        // do nothing
    }
}
