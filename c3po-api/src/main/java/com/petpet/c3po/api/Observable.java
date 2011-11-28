package com.petpet.c3po.api;

public interface Observable {

  void addObserver(Call callback);

  void removeObserver(Call callback);

  void notifyObservers(Object o);
}
