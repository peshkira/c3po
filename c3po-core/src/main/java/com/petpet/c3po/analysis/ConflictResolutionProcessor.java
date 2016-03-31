package com.petpet.c3po.analysis;

import com.petpet.c3po.analysis.conflictResolution.Rule;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.utils.Configurator;

import java.util.List;
import java.util.Map;

/**
 * Created by artur on 31/03/16.
 */
public class ConflictResolutionProcessor {

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    List<Rule> rules;

    public long resolve() {
        long result=0;
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        for (Rule r : rules) {
            persistence.update(r.getElement(), r.getFilter());
            Map<String, Object> map=persistence.getResult();
            if (map.get("count") !=null)
            result += (int) map.get("count");
        }
        return result;
    }
}


