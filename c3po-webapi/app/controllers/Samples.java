/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package controllers;

import com.petpet.c3po.analysis.RepresentativeAlgorithmFactory;
import com.petpet.c3po.analysis.RepresentativeGenerator;
import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.api.model.Element;
import com.petpet.c3po.api.model.helper.Filter;
import com.petpet.c3po.api.model.helper.FilterCondition;
import com.petpet.c3po.utils.Configurator;
import common.WebAppConstants;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class Samples extends Controller {

    public static Result index() {
        final List<String> names = Properties.getCollectionNames();
        return ok(samples.render(names));
    }

    public static Result getSamples(String alg, int size, String props) {
        Logger.debug("Received a getSamples call, sampling with alg " + alg + " size " + size + " props " + props);
        final PersistenceLayer persistence = Configurator.getDefaultConfigurator().getPersistence();
        final Filter filter = Filters.getFilterFromSession();
        final RepresentativeGenerator sg = new RepresentativeAlgorithmFactory().getAlgorithm(alg);
        sg.setFilter(filter);

        if (alg.equals("distsampling")) {
            final String[] properties = props.split(",");
            final HashMap<String, Object> options = new HashMap<String, Object>();
            options.put("properties", Arrays.asList(properties));
            sg.setOptions(options);
        }
        if (alg.equals("sfd")) {
            List<String> list = Arrays.asList(props.split(","));
            HashMap<String, Object> options = new HashMap<String, Object>();
            options.put("pcoverage", list.get(0));
            options.put("tcoverage", list.get(1));
            options.put("threshold", list.get(2));
            options.put("proportion", list.get(3));


            Map<String, List<Integer>> binThresholds = new HashMap<String, List<Integer>>();
            List<Integer> bins = new ArrayList<Integer>();
            bins.add(5);
            bins.add(20);
            bins.add(40);
            bins.add(100);
            bins.add(1000);
            bins.add(10000);
            bins.add(10000000);
            bins.add(1000000000);
            binThresholds.put("size", bins);
            binThresholds.put("wordcount", bins);
            binThresholds.put("pagecount", bins);

            options.put("bins", binThresholds);


            options.put("location", "exports/" + session(WebAppConstants.SESSION_ID) + "_sfd_results.zip");
            List<String> tmp_props = new ArrayList<String>();
            for (int i = 4; i < list.size(); i++)
                tmp_props.add(list.get(i));
            options.put("properties", tmp_props);
            sg.setOptions(options);
        }

        List<Element> samples = new ArrayList<Element>();
        List<String> output = sg.execute(size);

        for (String uid : output) {
            Filter idFilter = new Filter();
            idFilter.addFilterCondition(new FilterCondition("uid", uid));
            Iterator<Element> iter = persistence.find(Element.class, idFilter);
            Element next = iter.next();
            if (next != null)
                samples.add(next);
        }
        return ok(play.libs.Json.toJson(samples));
    }

    public static Result exportResults() {
        Logger.debug("Exporting the template config file");
        String path = "exports/" + session(WebAppConstants.SESSION_ID) + "_sfd_results.zip";
        File file = new File(path);
        try {
            response().setContentType("application/zip");
            response().setHeader("Content-disposition", "attachment; filename=" + session(WebAppConstants.SESSION_ID) + "_sfd_results.zip");
            return ok(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return internalServerError(e.getMessage());
        }

    }

    public static Result indexAsJson() {
        response().setContentType("application/json");
        Map<String, String[]> queryString = request().queryString();
        Map<String, String[]> query = new HashMap<String, String[]>();
        query.putAll(queryString);

        String[] algArray = query.remove("alg");
        if (algArray == null || algArray.length == 0) {
            return badRequest("{error: \"No algorithm (alg) parameter provided. Use one of 'sizesampling', 'syssampling', 'distsampling'\"}");
        }

        String alg = algArray[0];

        if (!RepresentativeAlgorithmFactory.isValidAlgorithm(alg)) {
            return badRequest("{error: \"The provided algorithm is not supported. Use one of 'sizesampling', 'syssampling', 'distsampling'\"}");
        }

        String[] countArray = query.remove("count");
        if (countArray == null || countArray.length == 0) {
            return badRequest("{error: \"No count parameter provided\"}");
        }
        int size = -1;
        try {
            size = Integer.valueOf(countArray[0]);
        } catch (NumberFormatException e) {
            return badRequest("{error: \"The provided count parameter is invalid. Please provide a positive integer\"}");
        }

        if (size <= 0) {
            return badRequest("{error: \"The provided count parameter is invalid. Please provide a positive integer\"}");
        }

        RepresentativeGenerator gen = new
                RepresentativeAlgorithmFactory().getAlgorithm(alg);

        if (alg.equals("distsampling")) {
            String[] propertyArray = query.remove("property");
            if (propertyArray == null || propertyArray.length == 0) {
                return badRequest("{error: \"Cannot use distsampling without definining at least one property\"}");
            }

            final HashMap<String, Object> options = new HashMap<String, Object>();
            options.put("properties", Arrays.asList(propertyArray));
            gen.setOptions(options);
        }

        List<Element> samples = getSamples(gen, query, size);
        return ok(play.libs.Json.toJson(samples));
    }

    private static List<Element> getSamples(RepresentativeGenerator gen, Map<String, String[]> query, int count) {
        Configurator configurator = Configurator.getDefaultConfigurator();
        PersistenceLayer pl = configurator.getPersistence();
        Filter filter = Filters.getFilterFromQuery(query);
        gen.setFilter(filter);

        List<String> ids = gen.execute(count);
        System.out.println(ids.size());
        Filter idFilter = new Filter();
        for (String id : ids) {
            idFilter.addFilterCondition(new FilterCondition("uid", id));
        }

        Iterator<Element> iter = pl.find(Element.class, idFilter);
        List<Element> elements = new ArrayList<Element>();
        while (iter.hasNext()) {
            elements.add(iter.next());
        }

        return elements;
    }


}
