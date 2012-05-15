package com.petpet.c3po.command;

import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.petpet.c3po.dao.LocalPersistenceLayer;

public class AnonymizeCommand implements Command {

  private static final Logger LOG = LoggerFactory.getLogger(AnonymizeCommand.class);

  private Option[] options;

  private long time = -1;

  private LocalPersistenceLayer pLayer;

  public AnonymizeCommand(Option[] o) {
    this.options = o;
  }

  @Override
  public void execute() {
    LOG.info("starting anonymization command");
    long start = System.currentTimeMillis();
//
//    this.pLayer = new LocalPersistenceLayer();
//    final Configurator configurator = new Configurator(this.pLayer);
//    configurator.configure();
//
//    final String name = this.getCollectionName();
//    final DigitalCollection collection = this.getCollection(name);
////    final String a = "anonymized_";
//    if (collection != null) {
//      this.pLayer.getEntityManager().getTransaction().begin();
//      int res = this.pLayer
//          .getEntityManager()
//          .createNativeQuery(
//              "UPDATE Element SET name = 'anonymized_' || id, uid = 'anonymized_' || id WHERE collection_id = :id")
//          .setParameter("id", collection.getId()).executeUpdate();
//      this.pLayer.getEntityManager().getTransaction().commit();
//
//      this.pLayer.handleUpdate(DigitalCollection.class, collection);
//
//      LOG.info("Anonymized {} elements", res);
//
//    } else {
//      LOG.error("No collection with the specified name {}", name);
//    }

    long end = System.currentTimeMillis();
    this.time = end - start;

  }

  private String getCollectionName() {
    for (Option o : this.options) {
      if (o.getArgName().equals(CommandConstants.COLLECTION_ID_ARGUMENT)) {
        return o.getValue();
      }
    }

    LOG.warn("No collection identifier found");
    return null;
  }

//  private DigitalCollection getCollection(final String name) {
//    PreparedQueries pq = new PreparedQueries(this.pLayer.getEntityManager());
//
//    DigitalCollection collection = null;
//    try {
//      collection = pq.getCollectionByName(name);
//    } catch (NoResultException e) {
//      // swallow
//    }
//
//    return collection;
//
//  }

  @Override
  public long getTime() {
    return this.time;
  }

}
