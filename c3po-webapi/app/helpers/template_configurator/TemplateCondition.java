package helpers.template_configurator;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class TemplateCondition {
	public TemplateCondition(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	public TemplateCondition()
	{}
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
	@Override
	public int hashCode() {
		int result=0;
		result+=key.length();
		result+=value.length();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj==null)
			return false;
		if (obj.getClass()!=getClass())
			return false;
		TemplateCondition that=(TemplateCondition) obj;
		if (key.equals(that.key) && (value.equals(that.value)))
			return true;
		return false;
	}

}
