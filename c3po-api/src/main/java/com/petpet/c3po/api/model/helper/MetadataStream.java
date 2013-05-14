package com.petpet.c3po.api.model.helper;

/**
 * A simple wrapper around an input stream that holds the fileName (or some
 * human readable identifier) of the stream. Used to output more verbose logs
 * and information to the user.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public class MetadataStream {

  /**
   * Some human readable name of the digital object that this meta data stream
   * describes.
   */
  private String fileName;

  /**
   * The meta data that was read from the file or source stream.
   */
  private String data;

  /**
   * Sets the passed variables.
   * 
   * @param fileName
   *          a human readable identifier of the object that this metadata
   *          stream describes.
   * @param data
   *          the actial meta data in string form.
   */
  public MetadataStream(String fileName, String data) {
    this.fileName = fileName;
    this.data = data;
  }

  public String getFileName() {
    return this.fileName;
  }

  public String getData() {
    return this.data;
  }
}
