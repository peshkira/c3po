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
import template_configurator.configuration;

public class PropertySetTemplate {
	private static String[] defaultProps ={"content_type", "created", "valid"}; //{ "mimetype", "format", "format_version", "valid", "wellformed","creating_application_name", "created" };
	public static final String USER_PROPERTIES = System.getProperty( "user.home" ) + File.separator + ".c3po.template_config";
	@SuppressWarnings("rawtypes")
	public static void setProps(Filter filter){
		Application.PROPS=defaultProps;
		
		if (filter==null){
			return;
		}
		
		HashMap<List, List> templates = loadConfig();  
		Map mapFilter=filterToString(filter);
		configuration config=new configuration();
		config.loadFromFile(new File( USER_PROPERTIES ));
		String[] props=config.getProps(mapFilter);
		if (props.length!=0)
			Application.PROPS=props;
		//for(List l: templates.keySet()){
		//	if (contains(listFilter, l)) 
		//	{
		//		Application.PROPS=(String[])templates.get(l).toArray();
		//		return;
		//	}
		//}
	}

	private static Boolean contains(List listFilter, List listTemplate) {
		Boolean result=true;
		for (String s: (List<String>) listTemplate){
			Boolean contains=false;
			for (String sf: (List<String>) listFilter){
				if (sf.contains(s)){
					contains=true;
				}
			}
			if (!contains)
				result=false;
		}
		return result;
	}
	
	public static HashMap<List, List> loadConfig(File config) throws FileNotFoundException, IOException{
		LinkedHashMap<List, List> result=new LinkedHashMap<>();

		final File f = config;
		//Properties props=new Properties();
		if ( f.exists() && f.isFile() ) {
			Logger.debug( "Found user defined properties, loading." );
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.contains("#"))
						continue;
					if (line.contains("=")){
						String[] tmp = line.split("=");
						String key=tmp[0];
						String value=tmp[1];
						key=key.replace(" ", "");
						value=value.replace(" ", "");
						List<String> filter_values = Arrays.asList(key.split(","));
						List<String> histograms= Arrays.asList(value.split(","));
						Collections.sort(filter_values);
						Collections.sort(histograms);
						result.put(filter_values, histograms);
						Logger.debug(key + " => " + value);
					}

				}
			} 


		}
		return result;
	}

	public static HashMap<List, List> loadConfig(){
		LinkedHashMap<List, List> result=new LinkedHashMap<>();

		final File f = new File( USER_PROPERTIES );
		//Properties props=new Properties();
		if ( f.exists() && f.isFile() ) {
			Logger.debug( "Found user defined properties, loading." );
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.contains("#"))
						continue;
					if (line.contains("=")){
						String[] tmp = line.split("=");
						String key=tmp[0];
						String value=tmp[1];
						key=key.replace(" ", "");
						value=value.replace(" ", "");
						List<String> filter_values = Arrays.asList(key.split(","));
						List<String> histograms= Arrays.asList(value.split(","));
						Collections.sort(filter_values);
						Collections.sort(histograms);
						result.put(filter_values, histograms);
						Logger.debug(key + " => " + value);
					}

				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
		return result;
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
				value=low.toString()+"-"+high.toString();
			}
			result.put(s, value.toString());
			//list.add(s+"."+value);
		}
		//Collections.sort(list);

		return result;

	}
}
