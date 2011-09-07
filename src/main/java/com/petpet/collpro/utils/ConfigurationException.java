package com.petpet.collpro.utils;

public class ConfigurationException extends Exception {

    private static final long serialVersionUID = 8062740465216910277L;
    
    public ConfigurationException() {
        super();
    }
    
    public ConfigurationException(String msg) {
        super(msg);
    }
    
    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
