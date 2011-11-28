package com.petpet.c3po.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(SimpleErrorHandler.class);
    
    private boolean valid;
    
    public SimpleErrorHandler() {
        this.valid = true;
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        LOG.error("Error: {}", e.getMessage());
        this.valid = false;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        LOG.error("Fatal Error: {}", e.getMessage());
        this.valid = false;

    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        LOG.error("Warning: {}", e.getMessage());

    }
    
    public void reset() {
        this.valid = true;
    }
    
    public boolean isValid() {
        return this.valid;
    }

}
