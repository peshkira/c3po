package template_configurator;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
@Root
public class configuration {
	public List<filter> getFilters() {
		return filters;
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

}
