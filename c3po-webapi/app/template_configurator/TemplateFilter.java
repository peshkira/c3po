package template_configurator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
@Root
public class TemplateFilter {
	
	public String getTemplate_ID() {
		return template_ID;
	}
	public void setTemplate_ID(String template_ID) {
		this.template_ID = template_ID;
	}
	public List<TemplateCondition> getConditions() {
		return conditions;
	}
	public void setConditions(List<TemplateCondition> conditions) {
		this.conditions = conditions;
	}
	@Element
	String template_ID;
	@ElementList
	List<TemplateCondition> conditions;
	@Override
	public int hashCode() {
		int result=0;
		result+=template_ID.length();
		for (TemplateCondition cond:conditions){
			result+=cond.hashCode();
		}
		return result;
	}
	public Map toMap(){
		Map<String, String> result=new HashMap<String,String>();
		
		for (TemplateCondition tc: conditions){
			result.put(tc.getKey(),tc.getValue());
		}	
		return result;
	}
	
	public boolean equals(Object obj) {
		if (obj==null)
			return false;
		
		if (obj == this)
			return true;
		
		if (!(obj instanceof Map))
			return false;
		Map<String,String> thatMap=(Map<String,String>) obj;
		Map<String, String> thisMap=this.toMap();
		if (thatMap.size()!= thisMap.size())
			return false;
		
		Iterator<Entry<String, String>> iteratorThis = thisMap.entrySet().iterator();
		
		while(iteratorThis.hasNext()){
			Entry<String, String> next = iteratorThis.next();
			String key = next.getKey();
			String value = next.getValue();
			if (value==null){
				if (thatMap.get(key)!=null || !thatMap.containsKey(key))
					return false;
			}
			else {
				if (!value.equals(thatMap.get(key)))
						return false;
				
			}
			
			
		}
		return true;
		
		/*TemplateFilter that=(TemplateFilter) obj;
		if (template_ID.equals(that.template_ID)){
			ArrayList<TemplateCondition> tmp=new ArrayList<TemplateCondition>(conditions);
			ArrayList<TemplateCondition> tmp2=new ArrayList<TemplateCondition>(that.conditions);
			tmp.removeAll(that.conditions);
			tmp2.removeAll(conditions);
			if (tmp.isEmpty() && tmp2.isEmpty())
				return true;
		}
		return false;*/
	}

}
