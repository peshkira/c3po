package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.api.model.helper.Filter;

import com.petpet.c3po.api.model.helper.FilterCondition;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import template_configurator.TemplateController;

public class Templates extends Controller {

	public static final String USER_PROPERTIES = System.getProperty( "user.home" ) + File.separator + ".c3po.template_config";
	static TemplateController templateController =new TemplateController(new File( USER_PROPERTIES ));
	private static String[] defaultProps ={"content_type", "created","valid"}; //{ "mimetype", "format", "format_version", "valid", "wellformed","creating_application_name", "created" };

	public static Result importTemplate() {
		  play.mvc.Http.MultipartFormData body = request().body().asMultipartFormData();
		  play.mvc.Http.MultipartFormData.FilePart fileUploaded = body.getFile("fileUpload");
		  if (fileUploaded != null) {
		    String fileName = fileUploaded.getFilename();
		    String contentType = fileUploaded.getContentType(); 
		    File file = fileUploaded.getFile();
	    	try {
				updateConfig(file);
			} catch (Exception e) {
				Logger.debug("Provided template is not valid");
				Logger.debug(e.getMessage());
				return notFound("Provided template is not valid");
			} 
		   Logger.debug("Template was imported successfully");
		    return redirect(routes.Export.index());
		  } else {
		    flash("error", "Missing file");
		    return redirect(routes.Application.index());    
		  }
	}
	
	public static List<String> getTemplates(){
		return templatesToString();
		
	}
	public static Result exportTemplate(){
		Logger.debug("Exporting the template config file");
		String path = System.getProperty( "user.home" ) + File.separator + ".c3po.template_config";
	    File file = new File(path);
	    try {
	      return ok(new FileInputStream(file));
	    } catch (FileNotFoundException e) {
	      return internalServerError(e.getMessage());
	    }
		
	}
	
	public static String getCurrentTemplate(){
		Filter filter = FilterController.getFilterFromSession();
		return getCurrentTemplate(filter);
	}


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
		if (filter!=null) {
			List<FilterCondition> fcs = filter.getConditions();
			for (FilterCondition fc : fcs) {
				if (!fc.getField().equals("collection"))
					result.put(fc.getField(),  (fc.getValue()==null)? "Unknown" : fc.getValue().toString());
			}
		}
		return result;

	}
}
