package com.petpet.c3po.utils;

import javax.persistence.NoResultException;

import com.petpet.c3po.api.dao.PersistenceLayer;
import com.petpet.c3po.common.Constants;
import com.petpet.c3po.datamodel.ValueSource;

public final class DBHelper {

  private static PersistenceLayer pl;

  public static void init(PersistenceLayer pl) {
    DBHelper.pl = pl;
  }

  public static ValueSource getValueSource(String toolname, String version) {
    ValueSource s = null;
    try {
     s = pl.getEntityManager().createNamedQuery(Constants.VALUE_SOURCE_BY_NAME_AND_VERSION, ValueSource.class)
          .setParameter("name", toolname).setParameter("version", version).getSingleResult();
    } catch (final NoResultException e) {
      s = (ValueSource) pl.handleCreate(ValueSource.class, new ValueSource(toolname, version));
    }

    return s;
  }

  private DBHelper() {

  }

}
