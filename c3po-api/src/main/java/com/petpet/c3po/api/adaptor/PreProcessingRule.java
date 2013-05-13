package com.petpet.c3po.api.adaptor;

/**
 * A pre processing rule is a special processing rule that should be applied
 * before (or during ) meta data processing in an adaptor. Each adaptor gets a
 * list of {@link PreProcessingRule} object and has to pass each parsed raw
 * record to each rule obtained via the
 * {@link AbstractAdaptor#getPreProcessingRules()} method. It is up to the
 * adaptor implementation to decide, whether or not to apply the rules.
 * 
 * @author Petar Petrov <me@petarpetrov.org>
 * 
 */
public interface PreProcessingRule extends ProcessingRule {

  /**
   * Returns true if the property and value should be skipped for some reason.
   * 
   * The adaptors should invoke this method for each raw record and skip the
   * meta data record if the method returns true.
   * 
   * @param property
   *          the property of the record.
   * @param value
   *          the value for the given property.
   * @param status
   *          the status if it is known.
   * @param tool
   *          the tool that provides this record.
   * @param version
   *          the version of the tool that provides this record.
   * @return true if the record should be skipped, false otherwise.
   */
  boolean shouldSkip(String property, String value, String status, String tool, String version);

}
