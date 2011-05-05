package com.petpet.collpro;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;
import org.eclipse.persistence.sessions.UnitOfWork;

public class SchemaSessionCustomizer implements SessionCustomizer {
    @Override
    public void customize(Session session) throws Exception {

        session.getEventManager().addListener(new SessionEventAdapter() {
            @Override
            public void postConnect(SessionEvent event) {

            }


            @Override
            public void postLogin(SessionEvent event) {
                System.out.println("this is triggered after a login");

                event.getSession().getLogin();
                String schemaName = (String) event.getSession().getProperty("schema.name");
                if (schemaName != null && !schemaName.isEmpty()) {
                    UnitOfWork unit = event.getSession().acquireUnitOfWork();

                    System.out.println("creating schema");
                    try {
                        unit.executeNonSelectingSQL("CREATE SCHEMA " + schemaName);
                    }
                    catch (Exception e) {
                        // we know this;
                    }
                    System.out.println("setting search_path");
                    unit.commitAndResumeOnFailure();

                    unit.executeNonSelectingSQL("SET SEARCH_PATH TO " + schemaName);
                    unit.commit();
                }
            }

            @Override
            public void preLogin(SessionEvent event) {
                System.out.println("this is triggered before a login");
            }
        });


    }
}
