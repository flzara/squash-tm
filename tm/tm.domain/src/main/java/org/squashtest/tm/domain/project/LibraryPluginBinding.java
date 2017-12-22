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
package org.squashtest.tm.domain.project;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@DiscriminatorColumn(name = "LIBRARY_TYPE", discriminatorType = DiscriminatorType.STRING)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name="LIBRARY_PLUGIN_BINDING")
public abstract class LibraryPluginBinding  {

	@Id
	@Column(name = "PLUGIN_BINDING_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "library_plugin_binding_plugin_binding_id_seq")
	@SequenceGenerator(name = "library_plugin_binding_plugin_binding_id_seq", sequenceName = "library_plugin_binding_plugin_binding_id_seq", allocationSize = 1)
	private long id;

	@Column
	private String pluginId;


	@ElementCollection
	@CollectionTable(name = "LIBRARY_PLUGIN_BINDING_PROPERTY", joinColumns = @JoinColumn(name = "PLUGIN_BINDING_ID"))
	@MapKeyColumn(name = "PLUGIN_BINDING_KEY")
	@Column(name = "PLUGIN_BINDING_VALUE")
	private Map<String, String> properties = new HashMap<>(2);


	public LibraryPluginBinding(){
		super();
	}

	public LibraryPluginBinding(String pluginId){
		super();
		this.pluginId = pluginId;
	}

	public long getId() {
		return id;
	}


	public String getPluginId() {
		return pluginId;
	}


	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getProperty(String propertyName){
		return properties.get(propertyName);
	}

	public void setProperty(String propertyName, String propertyValue){
		properties.put(propertyName, propertyValue);
	}

	public void setProperties(Map<String, String> properties){
		this.properties = properties;
	}

	public Set<String> listProperties(){
		return properties.keySet();
	}

	public Map<String, String> getProperties(){
		return properties;
	}
}
