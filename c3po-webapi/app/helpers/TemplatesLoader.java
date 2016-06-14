package helpers;

import com.google.common.collect.Lists;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import controllers.Application;
import controllers.Filters;
import helpers.template_configurator.Template;
import helpers.template_configurator.TemplateFilter;
import helpers.template_configurator.TemplateProperty;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import play.Logger;

import java.io.File;
import java.util.*;

@Root
public class TemplatesLoader {
    static final String PATH_TEMPLATE_CONFIG = System.getProperty("user.home") + File.separator + ".c3po.template_config";
    static TemplatesLoader templatesLoader = new TemplatesLoader(new File(PATH_TEMPLATE_CONFIG));
    static List<String> userAddedProperties ;
    static String[] defaultProps = {};
    @ElementList
    List<TemplateFilter> filters;
    @ElementList
    static List<Template> templates;

    public TemplatesLoader(File file) {
        loadFromFile(file);
    }

    public TemplatesLoader() {
        userAddedProperties = new ArrayList<String>();
    }

    public static String getCurrentTemplate() {
        Filter filter = Filters.getFilterFromSession();
        return getCurrentTemplate(filter);
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    @SuppressWarnings("rawtypes")
    public static void setProps(Filter filter) {
        if (!templatesLoader.isSet()) {
            templatesLoader.loadFromFile(new File(PATH_TEMPLATE_CONFIG));
        }

        Map mapFilter = filterToString(filter);

        List<String> templateProperties = templatesLoader.getProps(mapFilter);
        if (templateProperties == null)
            templateProperties = new ArrayList<String>();
        if (userAddedProperties != null)
            templateProperties.addAll(userAddedProperties);
        if (templateProperties.size() != 0)
            Application.PROPS = templateProperties.toArray(new String[0]);
    }

    public static void resetTemplates(){
        templatesLoader = new TemplatesLoader(new File(PATH_TEMPLATE_CONFIG));

    }


    public static List<String> templatesToString() {
        return templatesLoader.toArrayString();
    }

    public static String getCurrentTemplate(Filter filter) {

        Map mapFilter = filterToString(filter);
        return templatesLoader.getCurrentTemplate(mapFilter);

    }

    public static Map filterToString(Filter filter) {
        Map<String, String> result = new TreeMap<String, String>();
        if (filter != null) {
            List<FilterCondition> fcs = filter.getConditions();
            for (FilterCondition fc : fcs) {
                if (!fc.getField().equals("collection"))
                    result.put(fc.getField(), (fc.getValue() == null) ? "Unknown" : fc.getValue().toString());
            }
        }
        return result;

    }

    public static void addUserDefinedGraph(String property) {
        if (!userAddedProperties.contains(property)) {
            userAddedProperties.add(property);
        }
    }

    public List<Template> getTemplates() {
        return templates;
     }


    public List<TemplateFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<TemplateFilter> filters) {
        this.filters = filters;
    }

    public void loadFromFile(File file) {
        Serializer serlzr = new Persister();
        TemplatesLoader configRead = null;
        userAddedProperties = new ArrayList<String>();
        try {
            configRead = serlzr.read(TemplatesLoader.class, file);
            if (configRead != null) {
                setFilters(configRead.getFilters());
                setTemplates(configRead.getTemplates());

            }
        } catch (Exception e) {
            Logger.debug("No template config file was found. Using default values");
        }


    }

    @Override
    public int hashCode() {
        int result = 0;
        for (TemplateFilter filt : filters)
            result += filt.hashCode();
        for (Template templt : templates)
            result += templt.hashCode();
        return result;
    }

    public List<String> getProps(Map<String, String> filter) {
        TemplateFilter tmp_filter = findFilter(filter);
        Template tmp_template = findTemplate(tmp_filter);
        List<String> result = getProperties(tmp_template);
        return result;
    }

    public String getCurrentTemplate(Map<String, String> filter) {
        TemplateFilter tmp_filter = findFilter(filter);
        Template template = findTemplate(tmp_filter);
        if (template != null)
            return template.getName();
        return "Default";

    }

    private static List<String> getProperties(Template tmp_template) {
        List<String> result = null;
        if (tmp_template == null)
            return Lists.newArrayList(getDefaultTemplate());

        result = new ArrayList<String>();
        for (TemplateProperty prop : tmp_template.getProperties())
            result.add(prop.getName());
        return result;
    }

    private Template findTemplate(TemplateFilter tmp_filter) {
        if (tmp_filter == null || templates == null)
            return null;
        for (Template tmpl : templates) {
            if (tmpl.getID().equals(tmp_filter.getTemplate_ID()))
                return tmpl;
        }
        return null;
    }

    private TemplateFilter findFilter(Map<String, String> filter) {
        if (filters != null) {
            for (TemplateFilter filt : filters) {
                if (filt.equals(filter))
                    return filt;
            }
        }
        return null;
    }

    public ArrayList<String> toArrayString() {
        ArrayList<String> result = new ArrayList<String>();
        if (templates != null) {
            for (Template t : templates) {
                result.add(t.toString());
            }
        }
        return result;
    }

    public boolean isSet() {
        return (templates != null && !templates.isEmpty());
    }

    public static void updateConfig(File file){
        templatesLoader.loadFromFile(file);

    }

    public static void updateConfig(){
        templatesLoader.loadFromFile(new File( PATH_TEMPLATE_CONFIG ));

    }

    public static String[] getDefaultTemplate() {
        if (templates != null) {
            for (Template tmpl : templates) {
                if (tmpl.getName().equals("Default_template")) {
                    List<String> result = getProperties(tmpl);
                    return result.toArray(new String[0]);
                }
            }
        }
        return defaultProps;

    }

}
