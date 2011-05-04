package com.petpet.collpro;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.petpet.collpro.datamodel.Characteristic;
import com.petpet.collpro.datamodel.Element;
import com.petpet.collpro.datamodel.StringCharacteristic;
import com.petpet.collpro.db.DBManager;

/**
 * Hello world!
 * 
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        Session session = DBManager.getInstance().getSession();
//        StringCharacteristic sc = new StringCharacteristic("Hello", "World");
//        Element e = new Element();
//        e.setName("Element");
//        session.beginTransaction();
//        session.save(sc);
//        session.save(e);
//        session.getTransaction().commit();
        
        Query query = session.createQuery("FROM CHARACTERISTIC");
        List<Characteristic<?>> list = query.list();
        for (Characteristic<?> c : list) {
            System.out.print(c.getValue());
        }
        session.flush();
        session.close();
        
    }
}
