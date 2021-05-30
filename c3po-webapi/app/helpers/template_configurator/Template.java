package helpers.template_configurator;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.*;
@Root
public class Template {
	@Element
	String ID;
	@Element
	String name;
	@Element
	String message;
	@ElementList
	List<TemplateProperty> properties;
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<TemplateProperty> getProperties() {
		return properties;
	}
	public void setProperties(List<TemplateProperty> properties) {
		this.properties = properties;
	}
	@Override
	public int hashCode() {
		int result=0;
		result+=ID.length();
		result+=name.length();
		for(TemplateProperty prop: properties){
			result+=prop.hashCode();
		}
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj==null)
			return false;
		if (obj.getClass()!=getClass())
			return false;
		Template that=(Template) obj;
		if (ID.equals(that.ID) && (name.equals(that.name))){
			ArrayList<TemplateProperty> tmp=new ArrayList<TemplateProperty>(properties);
			ArrayList<TemplateProperty> tmp2=new ArrayList<TemplateProperty>(that.properties);
			tmp.removeAll(that.properties);
			tmp2.removeAll(properties);
			if (tmp.isEmpty() && tmp2.isEmpty())
				return true;
		}
			
		return false;
	}
	public String toString(){
		return this.message;
	}

}
