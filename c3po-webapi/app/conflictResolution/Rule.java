package conflictResolution;

/**
 * Created by artur on 23/03/16.
 */
public class Rule {
    public String getRawText() {
        return rawText;
    }
    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    String rawText;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;
}
