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

import play.mvc.*;

public class Elements extends Controller {

//  public static Result index() {
//    return list();
//
//  }
//
//  public static Result list() {
//    String collection = session().get(WebAppConstants.CURRENT_COLLECTION_SESSION);
//
//    int batch = getQueryParameter("batch", 25);
//    int offset = getQueryParameter("offset", 0);
//
//    return listElements(collection, batch, offset);
//
//  }
//
//  private static int getQueryParameter(String parameter, int dflt) {
//    String[] strings = request().queryString().get(parameter);
//    if (strings == null || strings.length == 0) {
//      return dflt;
//    }
//
//    return Integer.parseInt(strings[0]);
//
//  }
//
//  public static Result show(String id) {
//    Logger.info("Select element with id: " + id);
//    final List<String> names = Application.getCollectionNames();
//    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
//    DBCursor cursor = pl.find(Constants.TBL_ELEMENTS, new BasicDBObject("_id", new ObjectId(id)));
//
//    if (cursor.count() == 0) {
//      Logger.info("Cursor selected " + cursor.count());
//      return notFound("No such element exists in the db.");
//    } else if (cursor.count() > 1) {
//      Logger.info("Cursor selected " + cursor.count());
//      return notFound("One or more objects with this id exist");
//    } else {
//
//      Element elmnt = DataHelper.parseElement(cursor.next(), pl);
//
//      return ok(element.render(names, elmnt));
//    }
//
//  }
//
//  public static Result listElements(String collection, int batch, int offset) {
//    final List<String> names = Application.getCollectionNames();
//
//    if (collection == null) {
//      return ok(elements.render(names, null));
//    }
//
//    final List<Element> result = new ArrayList<Element>();
//    final PersistenceLayer pl = Configurator.getDefaultConfigurator().getPersistence();
//
//    BasicDBObject query = new BasicDBObject();
//    query.put("collection", collection);
//    Filter filter = Application.getFilterFromSession();
//    if (filter != null) {
//      query = Application.getFilterQuery(filter);
//      System.out.println("Objects Query: " + query);
//    }
//
//    final DBCursor cursor = pl.getDB().getCollection(Constants.TBL_ELEMENTS).find(query).skip(offset).limit(batch);
//
//    Logger.info("Cursor has: " + cursor.count() + " objects");
//
//    while (cursor.hasNext()) {
//      final Element e = DataHelper.parseElement(cursor.next(), pl);
//
//      if (e.getName() == null) {
//        e.setName("missing name");
//      }
//
//      result.add(e);
//    }
//
//    return ok(elements.render(names, result));
//  }

}
