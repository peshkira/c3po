package com.petpet.c3po.dao;

import java.net.UnknownHostException;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.petpet.c3po.api.dao.PersistenceLayer;

public class LocalPersistenceLayer implements PersistenceLayer {

  private Mongo mongo;

  private DB db;

  public LocalPersistenceLayer(Map<String, String> config) {
    this.connect(config);
  }

  @Override
  public DB getDB() {
    return this.db;
  }

  public DB connect(Map<String, String> config) {
    this.close();

    try {
      this.mongo = new Mongo(config.get("host"), Integer.parseInt(config.get("port")));
      this.db = this.mongo.getDB(config.get("db.name"));

    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (MongoException e) {
      e.printStackTrace();
    }

    return this.db;
  }

  public void close() {
    if (this.mongo != null) {
      this.mongo.close();
    }
  }

}
