package com.petpet.c3po.api;

public class Message<T> {

  private T data;

  public Message(T data) {
    this.data = data;

  }

  public T getData() {
    return data;
  }
  
  public String toString() {
    return "Notification: " + ((data == null) ? "null" : data.toString());
  }

}
