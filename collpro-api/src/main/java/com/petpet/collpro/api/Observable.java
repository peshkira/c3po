package com.petpet.collpro.api;

public interface Observable {

  void addObserver(NotificationListener listener);

  void removeObserver(NotificationListener listener);

  void notifyObservers(Object o);
}
