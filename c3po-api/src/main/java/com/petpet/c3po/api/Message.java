package com.petpet.c3po.api;

public class Message<T> {
  
  public enum MessageType {
    FS_GATHERER, SSH_GATHERER, RODA_GATHERER, ESD_GATHERER, ROSETTA_GATHERER, SYSTEM;
  }

  private T data;
  
  private MessageType type;

  public Message(T data) {
    this.data = data;

  }

  public T getData() {
    return data;
  }

  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

  public String toString() {
    return "Notification: " + ((data == null) ? "null" : data.toString());
  }
}
