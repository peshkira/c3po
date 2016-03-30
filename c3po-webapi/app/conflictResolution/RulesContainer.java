package conflictResolution;

import java.util.List;

/**
 * Created by artur on 23/03/16.
 */
public class RulesContainer {
    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    List<Rule> rules;
}
