package template_configurator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	
	public boolean equals(Object obj) {
		if (obj==null)
			return false;
		
		if (obj instanceof Map){
			Map<String,String> thatMap=(Map<String,String>) obj;
			for (Entry<String,String> entry: thatMap.entrySet()){
				condition tmp_condition=new condition(entry.getKey(),entry.getValue());
				if (!conditions.contains(tmp_condition))
					return false;
			}
			return true;
		}
		
		if (obj.getClass()!=getClass())
			return false;
		filter that=(filter) obj;
		if (template_ID.equals(that.template_ID)){
			ArrayList<condition> tmp=new ArrayList<>(conditions);
			ArrayList<condition> tmp2=new ArrayList<>(that.conditions);
			tmp.removeAll(that.conditions);
			tmp2.removeAll(conditions);
			if (tmp.isEmpty() && tmp2.isEmpty())
				return true;
		}
		return false;
	}

}
