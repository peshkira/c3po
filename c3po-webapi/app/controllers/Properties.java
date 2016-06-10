package controllers;


import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.Property;
import com.petpet.c3po.api.model.helper.*;
import com.petpet.c3po.utils.Configurator;
import helpers.Distribution;
import helpers.Graph;
import helpers.PropertyValuesFilter;
import play.Logger;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static helpers.StringParser.DistibutionRangeValueToString;

public class Properties extends Controller {

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
        final List<String> names = Properties.getCollectionNames();

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
            List<String> names = Properties.getCollectionNames();
            return ok(play.libs.Json.toJson(names));
        }
        return badRequest("The accept header is not supported");
    }

    public static Graph interpretDistribution(Distribution d, String algorithm, String width){
        Graph result =null;
        if (d==null || d.getProperty()==null)
            return result;
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Property p = persistence.getCache().getProperty(d.getProperty());
        PropertyType pType=PropertyType.valueOf(p.getType());
        switch (pType){
            case INTEGER:
            case FLOAT:
                Distribution mergedDistribution = processNumericDistribution(d, algorithm, width);
                result = new Graph(mergedDistribution.getProperty(), mergedDistribution.getPropertyValues(), mergedDistribution.getPropertyValueCounts());

                break;
            case DATE:
            case BOOL:
            case STRING:
                result = new Graph(d.getProperty(), d.getPropertyValues(), d.getPropertyValueCounts());
                break;
        }
        return result;
    }

    private static Distribution processNumericDistribution(Distribution d, String algorithm, String width) {
        Double min = getMin(d.getPropertyValues());
        if (min < 0)
            min = 0.0;
        Double max = getMax(d.getPropertyValues());
        if (max < 0)
            max = 0.0;
        Double count = (double) d.getPropertyValues().size();
        int bin_width = 0;
        int bins_count = 0;
        if (width == null)
            width = "";
        if (algorithm == null)
            algorithm = "sqrt";

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
        if (min.equals(max)){  //TODO: fix this case
            bins_count=1;
            bin_width= max.intValue();
        }
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
                if (propertyValue < 0)
                    continue;
                int bin_id=0;
                if (bins_count!=1)
                    bin_id = (int) (propertyValue / bin_width);
                binDistribution[bin_id] += propertyValueCount;
            }
        }
        Map<String, Long> propertyDistributionResult = new HashMap<String, Long>();
        for (int i = 0; i < bins_count; i++) {
            String leftValue = String.valueOf(i * bin_width);
            String rightValue = String.valueOf((i + 1) * bin_width);
            String id = DistibutionRangeValueToString(leftValue, rightValue, algorithm, width);
            propertyDistributionResult.put(id, binDistribution[i]);
        }
        if (conflictedCount > 0)
            propertyDistributionResult.put("CONFLICT", conflictedCount);
        if (unknownCount > 0)
            propertyDistributionResult.put("Unknown", unknownCount);

        Distribution mergedDistribution = new Distribution();
        mergedDistribution.setProperty(d.getProperty());
        mergedDistribution.setType(d.getType());
        mergedDistribution.setPropertyDistribution(propertyDistributionResult);
        return mergedDistribution;
    }

    public static Distribution getDistribution(String property, Filter filter) {
        List<String> properties=new ArrayList<String>();
        properties.add(property);
        Map<String, Distribution> distributions = getDistributions(properties, filter);
        return distributions.get(property);
        /*Distribution result = new Distribution();
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Property p = persistence.getCache().getProperty(property);
        if (filter == null)
            filter = new Filter();
        Filter tmpFilter = Filters.normalize(filter);
        Map<String, Long> histogram = persistence.getValueHistogramFor(p, tmpFilter);
        result.setPropertyDistribution(histogram);
        result.setProperty(p.getKey());
        result.setType(p.getType());
        result.setFilter(filter);
        return result;*/
    }


    public static Map<String, Distribution> getDistributions(List<String> properties, Filter filter){
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        if (filter == null)
            filter = new Filter();
        Filter tmpFilter = Filters.normalize(filter);
        Map<String, Map<String, Long>> histograms = persistence.getHistograms(properties, tmpFilter);
        Map<String, Distribution> distibutions=new HashMap<String, Distribution>();
        Iterator<Map.Entry<String, Map<String, Long>>> iterator = histograms.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Map<String, Long>> next = iterator.next();
            String propertyName = next.getKey();
            Property p = persistence.getCache().getProperty(propertyName);
            Distribution d = new Distribution();
            d.setPropertyDistribution(next.getValue());
            d.setProperty(propertyName);
            d.setType(p.getType());
            d.setFilter(filter);
            distibutions.put(propertyName,d);
        }
        return distibutions;
    }


    public static Map<String, Long> getStatistics(Filter filter, String property) {
        PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        Map<String, Long> result = new HashMap<String, Long>();
        Property p = persistence.getCache().getProperty(property);
        if (p.getType().equals(PropertyType.INTEGER.toString()) || p.getType().equals(PropertyType.FLOAT.toString())) {
            List<String> properties=new ArrayList<String>();
            properties.add(property);
            Map<String, Distribution> distributions = getDistributions(properties, filter);
            Distribution distribution = distributions.get(property);
            return distribution.getPropertyDistribution();
        }
        return result;




       /* if (p.getType().equals(PropertyType.INTEGER.toString()) || p.getType().equals(PropertyType.FLOAT.toString())) {
            Logger.debug("Calculating numeric statistics for the property '" + property + "'");
            Filter tmpFilter= Filters.normalize(filter);
            NumericStatistics ns = persistence.getNumericStatistics(p, tmpFilter);
            result.put("average", ns.getAverage());
            result.put("min", ns.getMin());
            result.put("max", ns.getMax());
            result.put("sd", ns.getStandardDeviation());
            result.put("var", ns.getVariance());
            result.put("count", (double) ns.getCount());
            result.put("sum", ns.getSum());
   }
        return result;*/
    }


    public static Map<String, Long> getStatistics(String property) {
        Filter f = Filters.getFilterFromSession();
        return getStatistics(f, property);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        try {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
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
            List<String> names = Properties.getPropertyNames();
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

        Filter f = Filters.getFilterFromSession();  //I dont use getCollection(), because we need to update the collection value in the filter.
        List<FilterCondition> fcs = f.getConditions();
        for (FilterCondition fc : fcs) {
            if (fc.getField().equals("collection")) {
                fc.setValue(c);
                Filters.setFilterFromSession(f);
                return ok("The collection was changed successfully");
            }
        }
        f.addFilterCondition(new FilterCondition("collection", c));
        Filters.setFilterFromSession(f);
        return ok("The collection was changed successfully");
    }

    public static String getCollection() {
        Filter f = Filters.getFilterFromSession();
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
        PropertyValuesFilter f= getValues(p,a,w,null);
        return ok(play.libs.Json.toJson(f));
    }

    public static PropertyValuesFilter getValues(String property, String algorithm, String width, String selectedValue) {
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
            Distribution d = Properties.getDistribution(property, null);
            Graph graph = Properties.interpretDistribution(d, algorithm, width);
            PropertyValuesFilter result = new PropertyValuesFilter();
            result.setProperty(property);
            result.setType(d.getType());
            result.setValues(graph.getKeys());
            result.setSelected(selectedValue);
            if (selectedValue != null)
                result.setSelected(selectedValue);
            return result;
        }
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
