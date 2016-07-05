/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.analysis;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.utils.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Systematic sampling generator selects samples on random in a fair fashion.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 *
 */
public class SystematicSamplingRepresentativeGenerator extends RepresentativeGenerator {

    /**
     * Default logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SystematicSamplingRepresentativeGenerator.class);

    /**
     * The persistence layer.
     */
    private PersistenceLayer pl;

    /**
     * Creates the generator.
     */
    public SystematicSamplingRepresentativeGenerator() {
        this.pl = Configurator.getDefaultConfigurator().getPersistence();
    }

    /**
     * Selects 10 samples per default.
     */
    @Override
    public List<String> execute() {
        return this.execute(10);
    }

    @Override
    public List<String> execute(int limit) {
        LOG.info("Applying {} algorithm for representative selection", this.getType());

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
            int i = 0;
            while (result.size() < limit) {

                int offset = (int) (Math.random() * skip + result.size() * skip);
                LOG.debug("Trying to picking an element with index {}", offset);
                if (offset >= count)
                    continue;
                // skip the offset
                while (i < offset) {
                    i++;
                    if (!cursor.hasNext())
                        break;
                    cursor.next();
                }
                if (cursor.hasNext()) {
                    Element next = cursor.next();
                    result.add(next.getUid());
                    LOG.debug("Picked an element with index {}", offset);
                }
                //cursor = this.pl.find( Element.class, this.getFilter() );
            }

        }

        return result;
    }

    @Override
    public String getType() {
        return "systematic sampling";
    }

}
