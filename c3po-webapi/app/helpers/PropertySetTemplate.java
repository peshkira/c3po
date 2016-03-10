package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mongodb.BasicDBObject;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.DataHelper;

import controllers.Application;
import play.Logger;
import template_configurator.TemplateController;;

public class PropertySetTemplate {
	private static String[] defaultProps ={"content_type", "valid"}; //{ "mimetype", "format", "format_version", "valid", "wellformed","creating_application_name", "created" };
	public static final String USER_PROPERTIES = System.getProperty( "user.home" ) + File.separator + ".c3po.template_config";
	@SuppressWarnings("rawtypes")
	public static void setProps(Filter filter){
		if (!templateController.isSet())
			templateController.loadFromFile(new File( USER_PROPERTIES ));
		Application.PROPS=templateController.getDefaultTemplate();
		
		if (filter==null){
			return;
		}
		Map mapFilter=filterToString(filter);
		
		String[] props=templateController.getProps(mapFilter);
		if (props.length!=0)
			Application.PROPS=props;
	}
	static TemplateController templateController =new TemplateController(new File( USER_PROPERTIES ));
	public static void updateConfig(File file){
		templateController.loadFromFile(file);
		
	}
	public static void updateConfig(){
		templateController.loadFromFile(new File( USER_PROPERTIES ));
		
	}
	public static  List<String> templatesToString(){
		return templateController.toArrayString();
	}
	public static String getCurrentTemplate(Filter filter){
		Map mapFilter=filterToString(filter);
		return templateController.getCurrentTemplate(mapFilter);
		
	}
	
	

	public static Map filterToString(Filter filter){
		Map<String,String> result=new TreeMap<String,String>();


		DataHelper.init();
		BasicDBObject ref= DataHelper.getFilterQuery(filter);
		ref.removeField("collection");
		Map<String,String> map= ref.toMap(); 
		//List list=new ArrayList();									
		for(String s: map.keySet()){
			Object value=map.get(s);
			s=s.replace("metadata.", "");
			s=s.replace(".value", "");
			if (value instanceof BasicDBObject){
				Map<String,String> valueMap=((BasicDBObject) value).toMap();
				Object high = valueMap.get("$lte");
				Object low = valueMap.get("$gte");
				if (high == null || low==null)
					value =null;
				else
					value=low.toString()+"-"+high.toString();
			}
			if (value!=null)
				result.put(s, value.toString());
			//list.add(s+"."+value);
		}
		//Collections.sort(list);

		return result;

	}
}
