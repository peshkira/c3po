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
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.utils.DataHelper;

import controllers.Application;
import play.Logger;
import template_configurator.TemplateController;;

public class PropertySetTemplate {
	private static String[] defaultProps ={"content_type", "created","valid"}; //{ "mimetype", "format", "format_version", "valid", "wellformed","creating_application_name", "created" };
	public static final String USER_PROPERTIES = System.getProperty( "user.home" ) + File.separator + ".c3po.template_config";
	@SuppressWarnings("rawtypes")
	public static void setProps(Filter filter){
		if (!templateController.isSet())
			templateController.loadFromFile(new File( USER_PROPERTIES ));
		Application.PROPS=templateController.getDefaultTemplate();
		
		if (filter==null){
			return;
		}
		//Map mapFilter=filterToString(filter);
		
		//String[] props=templateController.getProps(mapFilter);
		//if (props.length!=0)
		//	Application.PROPS=props;
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
		
		//Map mapFilter=filterToString(filter);
		return null;// templateController.getCurrentTemplate(mapFilter);
		
	}
	public static Map filterToString(Filter filter){
		Map<String,String> result=new TreeMap<String,String>();

		List<FilterCondition> fcs=filter.getConditions();
		for (FilterCondition fc : fcs){
			if (!fc.getField().equals("collection"))
				result.put(fc.getField(), (String)fc.getValue());
		}
		return result;

	}
}
