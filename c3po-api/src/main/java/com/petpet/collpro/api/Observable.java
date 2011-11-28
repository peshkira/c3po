package com.petpet.collpro.api;

public interface Observable {

  void addObserver(Call callback);

  void removeObserver(Call callback);

  void notifyObservers(Object o);
}
