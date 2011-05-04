package com.petpet.collpro.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DBManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DBManager.class);

    private transient SessionFactory sessionFactory;
    
    private transient Session session;
    
    private static DBManager uniqueInstance;
    
    public static synchronized DBManager getInstance() {
        if (DBManager.uniqueInstance == null) {
            DBManager.uniqueInstance = new DBManager();
        }
        
        return DBManager.uniqueInstance;
    }
    
    public Session getSession() {
        if (this.session == null || !this.session.isOpen()) {
            this.session = this.createSessionFactory().openSession();
        }
        return this.session;
    }

    public void closeSession() {
        this.session.close();
        this.sessionFactory.close();
    }
    
    private SessionFactory createSessionFactory() {
        try {
            DBManager.LOGGER.debug("Create the new SessionFactory from hibernate.cfg.xml");
            this.sessionFactory = new Configuration().configure().buildSessionFactory();

        } catch (final Exception ex) {
            DBManager.LOGGER.error("Initial SessionFactory creation failed.");
            throw new ExceptionInInitializerError(ex);
        }
        
        return this.sessionFactory;
    }
    
    private DBManager() {
        
    }
}
