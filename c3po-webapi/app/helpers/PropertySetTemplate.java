package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.utils.*;

import controllers.Application;
import play.Logger;

public class PropertySetTemplate {
	private static String[] defaultProps = { "mimetype", "format", "format_version", "valid", "wellformed",
			"creating_application_name", "created" };
	public static final String USER_PROPERTIES = System.getProperty( "user.home" ) + File.separator + ".c3potemplateconfig";
	@SuppressWarnings("rawtypes")
	public static void setProps(Filter filter){
		//if (filter == null)

		HashMap<List, List> templates = loadConfig();    //colorspace.RBG, mimetype.html/text=mimetype, format, valid
		List listFilter=filterToString(filter);
		for(List l: templates.keySet()){
			if (contains(listFilter, l)) // listFilter.containsAll(l))
			{
				Application.PROPS=(String[])templates.get(l).toArray();
				return;
			}
		}
		Application.PROPS=defaultProps;
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
	public static List filterToString(Filter filter){
		String[] result=null;


		DataHelper.init();
		BasicDBObject ref= DataHelper.getFilterQuery(filter);
		ref.removeField("collection");
		HashMap<String,String> map=(HashMap<String, String>) ref.toMap(); 
		List list=new ArrayList();									
		for(String s: map.keySet()){
			Object value=map.get(s);
			s=s.replace("metadata.", "");
			s=s.replace(".value", "");
			if (value instanceof BasicDBObject){
				HashMap<String,String> valueMap=(HashMap<String, String>) ((BasicDBObject) value).toMap();
				Object high = valueMap.get("$lte");
				Object low = valueMap.get("$gte");
				value=low.toString()+"-"+high.toString();
			}
			list.add(s+"."+value);
		}
		Collections.sort(list);

		return list;

	}
}
