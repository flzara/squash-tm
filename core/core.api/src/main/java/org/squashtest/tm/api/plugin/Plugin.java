/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.api.plugin;

import java.util.Map;


/**
 * @author Gregory Fouquet
 *
 */
public interface Plugin {

	/**
	 * This plugin persistent, globally unique identifier. A good value would be the osgi service name of this plugin. Should not
	 * return null.
	 *
	 * @return
	 */
	String getId();


	/**
	 * Returns the version of this plugin.
	 *
	 * @return
	 */
	String getVersion();


	/**
	 * @return the file name (eg a .jar) from which comes this plugin
	 */
	String getFilename();

	/**
	 * returns the type of the plugin. This is a free string that should help the user to know what this plugin is about. It would
	 * be courteous to make it translated in the locale used in the current thread.
	 *
	 * @return
	 */
	String getType();


	/**
	 * <p>Declares which properties, if any, are used by this plugin for configuration purposes. This map should never be null.</p>
	 * <p>For each entry the key is the property name, and the value is the default value. If there is no default value for a property
	 * then blank or null is fine.</p>
	 * <p>Depending on what kind of plugin it is (see subinterfaces and/or implementation), these properties will be either global for global plugin,
	 * or local if this plugin has a specific scope (for instance, a per-project configuration).</p>
	 *
	 *  @deprecated <b>Deprecation notice</b> : from now on Squash TM will rather ask  {#getConfigurationUrl(EntityReference)}, from which one can reach the conf page of the plugin.
	 *
	 * @return a Map
	 */
	/**
	 * @return
	 */
	@Deprecated
	Map<String, String> getProperties();

	/**
	 * <p>
	 * 	Must return the URI path that lead to a configuration page for this plugin. Because the configuration might be context-dependant,
	 * 	(a prominent example is per-project dependant), an EntityReference is supplied as this context.
	 * </p>
	 *
	 * <p>
	 * 	May return null if no configuration is necessary.
	 * </p>
	 *
	 *
	 * @param context
	 * @return
	 */
	String getConfigurationPath(EntityReference context);

	/**
	 * <p>Same as {@link #validate(EntityReference)}, except that the configuration is supplied as a parameter by Squash TM instead of
	 * letting the plugin read the database for it. This hashmap contains the values for the parameters declared by {@link #getProperties()}.</p>
	 *
	 * @derecated <b>Deprecation notice</b> : just as for {@link #getProperties()}, Squash TM will no more care of the details of the configuration
	 * and this method will no more be invoked.
	 *
	 * @param reference to a given object
	 */
	@Deprecated
	void validate(EntityReference reference, Map<String, String> configuration) throws PluginValidationException;

	/**
	 * Asks the plugin to validate its current configuration stored in the database. As the configuration may be context-dependant,
	 * as in a per-project configuration for instance, an EntityReference to that context is supplied.
	 * The EntityReference can be null, or a project, a node etc. Must either succeed, or throw a {@link PluginValidationException}.
	 *
	 * @param reference to a given object
	 */
	void validate(EntityReference reference) throws PluginValidationException;



}
