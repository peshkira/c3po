package controllers;


import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.*;
import com.petpet.c3po.utils.Configurator;
import common.WebAppConstants;
import helpers.Distribution;
import helpers.PropertyValuesFilter;
import helpers.Statistics;
import helpers.StatisticsToPrint;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PropertyController extends Controller {

    public static String propertiesAsXml() {

        List<String> properties = getPropertyNames();

        final StringBuffer resp = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        resp.append("<properties>\n");

        for (String s : properties) {
            resp.append("<property name=\"" + s + "\" />\n");
        }
        resp.append("</properties>\n");
        response().setContentType("text/xml");
        return resp.toString();
    }

    public static String collectionsAsXml() {
        final List<String> names = PropertyController.getCollectionNames();

        final StringBuffer resp = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        resp.append("<collections>\n");

        for (String s : names) {
            resp.append("<collection name=\"" + s + "\" />\n");
        }
        resp.append("</collections>\n");
        response().setContentType("text/xml");
        return resp.toString();
    }

    public static List<String> getCollectionNames() {
        Logger.debug("Listing collection names");
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        List<String> collections = (List<String>) persistence.distinct(Element.class, "collection", new Filter());
        collections.add(0, "");
        Collections.sort(collections);
        return collections;
    }

    public static Result getCollections() {
        Logger.debug("Received a getCollections call");
        final String accept = request().getHeader("Accept");

        if (accept.contains("*/*") || accept.contains("application/xml")) {
            response().setContentType("text/xml");
            String result = collectionsAsXml();
            return ok(result);
        } else if (accept.contains("application/json")) {
            response().setContentType("application/json");
            List<String> names = PropertyController.getCollectionNames();
            return ok(play.libs.Json.toJson(names));
        }
        return badRequest("The accept header is not supported");
    }

    public static Distribution getDistribution(String property, Filter filter, String algorithm, String width) {
        Logger.debug("Calculating distrubution for the property '" + property + "'");
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Property p = persistence.getCache().getProperty(property);
        Distribution result = null;
        if (p.getType().equals(PropertyType.INTEGER.toString()) || p.getType().equals(PropertyType.FLOAT.toString())) {
            if (algorithm == null)
                algorithm = "sqrt";
            result = getBinDistribution(property, filter, algorithm, width);
        } else {
            result = getNonimalDistribution(property, filter);
        }
        return result;
    }

    public static Distribution getNonimalDistribution(String property, Filter filter) {
        Distribution result = new Distribution();
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Property p = persistence.getCache().getProperty(property);
        if (p == null)
            return result;
        if (filter == null)
            filter = new Filter();

        Filter tmpFilter = FilterController.normalize(filter);
        Map<String, Long> histogram = persistence.getValueHistogramFor(p, tmpFilter);
        result.setPropertyDistribution(histogram);
        result.setProperty(p.getKey());
        result.setType(p.getType());
        //getStatistics(filter, property);
        return result;
    }

    public static Distribution getNominalDistribution(String property) {
        Filter f = FilterController.getFilterFromSession();
        return PropertyController.getNonimalDistribution(property, f);
    }

    public static Map<String, Double> getStatistics(Filter filter, String property) {
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Map<String, Double> result = new HashMap<String, Double>();
        Property p = persistence.getCache().getProperty(property);
        if (p.getType().equals(PropertyType.INTEGER.toString()) || p.getType().equals(PropertyType.FLOAT.toString())) {
            Logger.debug("Calculating numeric statistics for the property '" + property + "'");
            Distribution distribution = PropertyController.getNominalDistribution(property);

            Map<String, Long> propertyDistribution = distribution.getPropertyDistribution();
            long conflictedCount = 0;
            long unknownCount = 0;
            List<Double> values = new ArrayList<Double>();
            for (Map.Entry<String, Long> entry : propertyDistribution.entrySet()) {
                String key = entry.getKey();
                Long propertyValueCount = entry.getValue();
                if (key.equals("Unknown")) {
                    unknownCount = propertyValueCount;
                } else if (key.equals("CONFLICT")) {
                    conflictedCount = propertyValueCount;
                } else {
                    double propertyValue = Double.parseDouble(key);
                    for (long i = 0; i < propertyValueCount; i++)
                        values.add(propertyValue);
                }
            }
            Statistics stats = new Statistics(values);

            result.put("average", stats.getMean());
            result.put("min", stats.getMin());
            result.put("max", stats.getMax());
            result.put("sd", stats.getStdDev());
            result.put("var", stats.getVariance());
            result.put("count", stats.getCount());
            result.put("sum", stats.getSum());
        }
        return result;
    }

    public static Map<String, Double> getStatistics(String property) {
        Filter f = FilterController.getFilterFromSession();
        return getStatistics(f, property);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Result getProperties() {
        Logger.debug("Received a getProperties call");
        final String accept = request().getHeader("Accept");

        if (accept.contains("*/*") || accept.contains("application/xml")) {
            response().setContentType("text/xml");
            String result = propertiesAsXml();
            return ok(result);
        } else if (accept.contains("application/json")) {
            response().setContentType("application/json");
            List<String> names = PropertyController.getPropertyNames();
            return ok(play.libs.Json.toJson(names));
        }
        return badRequest("The accept header is not supported");
    }

    public static Result getProperty(String name) {
        Logger.debug("Received a getProperty call");
        final String accept = request().getHeader("Accept");
        if (accept.contains("*/*") || accept.contains("application/json")) {
            PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
            Property property = persistence.getCache().getProperty(name);
            return ok(play.libs.Json.toJson(property));
        } else {
            return TODO;
        }
    }

    public static List<String> getPropertyNames() {
        Logger.debug("Listing property names");
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        List<String> names = persistence.distinct(Property.class, "_id", null);
        return names;
    }

    public static Result setCollection(String c) {
        Logger.debug("Received a setCollection call to '" + c + "'");
        final List<String> names = getCollectionNames();

        if (c == null || c.equals("") || !names.contains(c)) {
            return notFound("No collection '" + c + "' was found");
        }

        Filter f = FilterController.getFilterFromSession();  //I dont use getCollection(), because we need to update the collection value in the filter.
        List<FilterCondition> fcs = f.getConditions();
        for (FilterCondition fc : fcs) {
            if (fc.getField().equals("collection")) {
                fc.setValue(c);
                session().put(WebAppConstants.CURRENT_COLLECTION_SESSION, c);
                FilterController.setFilterFromSession(f);
                return ok("The collection was changed successfully");
            }
        }
        session().put(WebAppConstants.CURRENT_COLLECTION_SESSION, c);
        f.addFilterCondition(new FilterCondition("collection", c));
        FilterController.setFilterFromSession(f);
        return ok("The collection was changed successfully");
    }

    public static String getCollection() {
        Filter f = FilterController.getFilterFromSession();
        List<FilterCondition> fcs = f.getConditions();
        for (FilterCondition fc : fcs) {
            if (fc.getField().equals("collection")) {
                return fc.getValue().toString();
            }
        }
        return null;
    }

    public static Result getValuesUnconditional() {
        Logger.debug("Received a getValuesUnconditional call in filter");
        final DynamicForm form = play.data.Form.form().bindFromRequest();
        final String p = form.get("filter");
        final String a = form.get("alg");
        final String w = form.get("width");

        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Property property = persistence.getCache().getProperty(p);

        PropertyValuesFilter f;
        if (property.getType().equals(PropertyType.INTEGER.toString()) || property.getType().equals(PropertyType.FLOAT.toString())) {
            f = getNumericValues(p, null, a, w, null); //TODO: Debug this!
        } else {
            f = getNominalValues(p, null, null);
        }
        return ok(play.libs.Json.toJson(f));
    }

    public static PropertyValuesFilter getNominalValues(String property, Filter filter, String selectedValue) {
        Logger.debug("get property values filter for property " + property);
        if (property.equals("collection")) {
            PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
            Property p = persistence.getCache().getProperty(property);
            PropertyValuesFilter pvf = new PropertyValuesFilter();
            pvf.setProperty(p.getKey());
            pvf.setType(p.getType());
            List<String> collections = getCollectionNames();
            collections.remove(0);
            pvf.setValues(collections);
            pvf.setSelected(getCollection());
            return pvf;
        } else {
            Distribution d = PropertyController.getNonimalDistribution(property, filter);
            PropertyValuesFilter pvf = new PropertyValuesFilter();
            pvf.setProperty(d.getProperty());
            pvf.setType(d.getType());
            pvf.setValues(d.getPropertyValues());
            if (selectedValue != null)
                pvf.setSelected(selectedValue);
            return pvf;
        }
    }

    public static PropertyValuesFilter getNumericValues(String property, Filter filter, String algorithm, String width, String selectedValue) {
        Distribution mergedDistribution = PropertyController.getBinDistribution(property, filter, algorithm, width);
        PropertyValuesFilter result = new PropertyValuesFilter();
        result.setProperty(property);
        result.setType(mergedDistribution.getType());
        result.setValues(mergedDistribution.getPropertyValues());
        result.setSelected(selectedValue);
        if (selectedValue != null)
            result.setSelected(selectedValue);
        return result;
    }


    public static Distribution getBinDistribution(String propertyName, Filter filter, String algorithm, String width) {
        Distribution d = getNonimalDistribution(propertyName, filter);
        Double min = getMin(d.getPropertyValues());
        Double max = getMax(d.getPropertyValues());
        Double count = (double) d.getPropertyValues().size();
        int bin_width = 0;
        int bins_count = 0;
        if (width == null)
            width = "";
        if (algorithm.equals("fixed")) {
            bin_width = Integer.parseInt(width);
            bins_count = (int) ((max - min) / bin_width);
        } else if (algorithm.equals("sqrt")) {
            bins_count = (int) (Math.sqrt(count));
            bin_width = (int) ((max - min) / bins_count);
        } else if (algorithm.equals("sturge")) {
            bins_count = (int) (Math.log(count) / Math.log(2) + 1);
            bin_width = (int) ((max - min) / bins_count);
        }
        bins_count++;
        long[] binDistribution = new long[bins_count];
        Map<String, Long> propertyDistribution = d.getPropertyDistribution();
        long conflictedCount = 0;
        long unknownCount = 0;
        for (Map.Entry<String, Long> entry : propertyDistribution.entrySet()) {
            String key = entry.getKey();
            Long propertyValueCount = entry.getValue();
            if (key.equals("Unknown")) {
                unknownCount = propertyValueCount;
            } else if (key.equals("CONFLICT")) {
                conflictedCount = propertyValueCount;
            } else {
                double propertyValue = Double.parseDouble(key);
                int bin_id = (int) (propertyValue / bin_width);
                binDistribution[bin_id] += propertyValueCount;
            }
        }
        Map<String, Long> propertyDistributionResult = new HashMap<String, Long>();
        for (int i = 0; i < bins_count; i++) {
            String leftValue = String.valueOf(i * bin_width);
            String rightValue = String.valueOf((i + 1) * bin_width);
            String id = leftValue + " - " + rightValue + " |" + algorithm + width;
            propertyDistributionResult.put(id, binDistribution[i]);
        }
        if (conflictedCount > 0)
            propertyDistributionResult.put("CONFLICT", conflictedCount);
        if (unknownCount > 0)
            propertyDistributionResult.put("Unknown", unknownCount);

        Distribution mergedDistribution = new Distribution();
        mergedDistribution.setProperty(propertyName);
        mergedDistribution.setType(d.getType());
        mergedDistribution.setPropertyDistribution(propertyDistributionResult);
        return mergedDistribution;
    }

    public static double getMin(List<String> values) {
        double result = Double.MAX_VALUE;
        for (String value : values) {
            try {
                double v = Double.parseDouble(value);
                if (result > v)
                    result = v;
            } catch (NumberFormatException e) {
            }
        }
        return result;
    }

    public static double getMax(List<String> values) {
        double result = Double.MIN_VALUE;
        for (String value : values) {
            try {
                double v = Double.parseDouble(value);
                if (result < v)
                    result = v;
            } catch (NumberFormatException e) {
            }
        }
        return result;
    }


    static Object getTypedValue(String val) {
        Logger.debug("Retrieving typedValue of '" + val + "'");
        Object value = null;
        try {
            value = Long.parseLong(val);
        } catch (NumberFormatException e) {
        }
        if (val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("true")) {
            value = new Boolean(true);
        } else if (val.equalsIgnoreCase("no") || val.equalsIgnoreCase("false")) {
            value = new Boolean(false);
        }
        if (value == null) {
            value = val;
        }
        return value;
    }
}
