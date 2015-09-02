package template_configurator;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class condition {
	public condition(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	@Element
	String key;
	@Element
	String value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
