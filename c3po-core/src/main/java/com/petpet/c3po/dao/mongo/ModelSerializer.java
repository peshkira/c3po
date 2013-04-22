package com.petpet.c3po.dao.mongo;

import com.mongodb.DBObject;

public interface ModelSerializer {

  DBObject serialize(Object object);
}
