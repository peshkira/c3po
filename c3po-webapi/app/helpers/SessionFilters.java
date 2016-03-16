package helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.petpet.c3po.utils.Configurator;

import play.Logger;

import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;

public class SessionFilters {
	final static HashMap<String, Filter> map=new HashMap<String, Filter>();
	public static Filter getFilter(String session){
		Filter result = map.get(session);
		if (result==null){
			result=new Filter();
			addFilter(session, result);
		}
		
		/*List<String> strings=new ArrayList<String>();
		for( FilterCondition fc: result.getConditions()){
			strings.add(fc.getField()+":"+fc.getValue().toString());
		}
		Logger.debug(session + " - "+strings.toString());*/
		
		return result;
	};
	public static void addFilter(String session,Filter filter){
		map.put(session, filter);
	}
	public static void clean(){
		map.clear();
	}
}
	


