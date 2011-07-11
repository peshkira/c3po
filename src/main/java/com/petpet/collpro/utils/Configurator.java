package com.petpet.collpro.utils;

public final class Configurator {

    private static Configurator uniqueInstance;
    
    public synchronized Configurator getInstance() {
        if (Configurator.uniqueInstance == null) {
            Configurator.uniqueInstance = new Configurator();
        }
        
        return Configurator.uniqueInstance;
    }
    
    public void configure() {
        //TODO load known properties into memory
        //TODO load properties files and setup preferences
    }
    
    private Configurator() {}
}
