package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import com.petpet.c3po.datamodel.Filter;
import com.petpet.c3po.api.model.helper.Filter;

import controllers.routes;
import helpers.PropertySetTemplate;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class Templates extends Controller {
	
	public static Result importTemplate() {
		  play.mvc.Http.MultipartFormData body = request().body().asMultipartFormData();
		  play.mvc.Http.MultipartFormData.FilePart fileUploaded = body.getFile("fileUpload");
		  if (fileUploaded != null) {
		    String fileName = fileUploaded.getFilename();
		    String contentType = fileUploaded.getContentType(); 
		    File file = fileUploaded.getFile();
	    	try {
				PropertySetTemplate.updateConfig(file);
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
		return PropertySetTemplate.templatesToString(); 
		
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
		return PropertySetTemplate.getCurrentTemplate(filter);
	}
	

}
