package com.petpet.collpro.api;

import java.util.List;
import java.util.Map;

import com.petpet.collpro.api.utils.ConfigurationException;

public interface ITool extends Observable {

	/**
	 * Starts the tool. This method should be called after configure was called,
	 * or addParameter if the tool needs some specific configuration.
	 */
	void execute();

	/**
	 * Sets the configuration parameters of the tool. For instance the
	 * collection on which the gatherer will work, the file path, eventually
	 * server config, etc. This method has to be called before the execute()
	 * call or the configuration should be done via the addParameter(String,
	 * Object) method.
	 * 
	 * @param configuration
	 *            the configuration map.
	 */
	void configure(Map<String, Object> configuration) throws ConfigurationException;

	/**
	 * Adds a configuration parameter for the tool. This is a convenience method
	 * for the tool configuration as it makes it easier to add the configuration
	 * parameters. However, you have to make sure all obligatory parameters
	 * should be added.
	 * 
	 * @param key
	 *            the name of the parameter.
	 * @param value
	 *            the value of the parameter
	 * @return returns the ITool for convenience.
	 */
	ITool addParameter(String key, Object value);

	/**
	 * Retrieves a list of all keys of the parameters that may be used by this
	 * tool.
	 * 
	 * @return the list with parameters.
	 */
	List<String> getConfigParameters();

	/**
	 * Retrieves a list of all mandatory parameters that must be set in oder to
	 * execute the tool successfully, or an empty list if no parameters are
	 * mandatory.
	 * 
	 * @return the list with mandatory paramateres.
	 */
	List<String> getMandatoryParameters();

}
