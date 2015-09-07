package template_configurator;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
@Root
public class filter {
	
	public String getTemplate_ID() {
		return template_ID;
	}
	public void setTemplate_ID(String template_ID) {
		this.template_ID = template_ID;
	}
	public List<condition> getConditions() {
		return conditions;
	}
	public void setConditions(List<condition> conditions) {
		this.conditions = conditions;
	}
	@Element
	String template_ID;
	@ElementList
	List<condition> conditions;
	@Override
	public int hashCode() {
		int result=0;
		result+=template_ID.length();
		for (condition cond:conditions){
			result+=cond.hashCode();
		}
		return result;
	}
	

}
