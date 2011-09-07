package com.petpet.collpro.utils;

import javax.swing.event.ChangeListener;

public interface Observable {

    void addObserver(ChangeListener listener);
    
    void removeObserver(ChangeListener listener);
    
    void notifyObservers(Object e);
}
