package template_configurator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.petpet.c3po.datamodel.Filter;
@Root
public class configuration {
	public List<filter> getFilters() {
		return filters;
	}
	public void loadFromFile(File file){
		Serializer serlzr = new Persister();
		configuration configRead=null;
		try {
			configRead = serlzr.read(configuration.class, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (configRead!=null){
			setFilters(configRead.getFilters());
			setTemplates(configRead.getTemplates());
		}
		
	}
	public void setFilters(List<filter> filters) {
		this.filters = filters;
	}
	public List<template> getTemplates() {
		return templates;
	}
	public void setTemplates(List<template> templates) {
		this.templates = templates;
	}
	@ElementList
	List<filter> filters;
	@ElementList
	List<template> templates;
	@Override
	public int hashCode() {
		int result=0;
		for(filter filt: filters)
			result+=filt.hashCode();
		for(template templt: templates)
			result+=templt.hashCode();
		return result;
	}
	public String[] getProps(Map<String,String> filter){
		filter tmp_filter= find_corresponding_template_filter(filter);
		template tmp_template=find_template(tmp_filter);
		List<String> result=get_list_of_properties(tmp_template);
		return result.toArray(new String[0]);
	}
	private List<String> get_list_of_properties(template tmp_template) {
		List<String> result=new ArrayList<String>();
		if (tmp_template==null)
			return result;
		for(property prop: tmp_template.properties)
			result.add(prop.name);
		return result;
	}
	private template find_template(filter tmp_filter) {
		if (tmp_filter==null)
			return null;
		for(template tmpl: templates){
			if (tmpl.ID.equals(tmp_filter.template_ID))
				return tmpl;
		}
		return null;
	}
	private filter find_corresponding_template_filter(Map<String,String>  filter) {
		
		for(filter filt: filters){
			if (filt.equals(filter))
				return filt;
		}
		return null;
	}

}
