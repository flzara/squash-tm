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
package org.squashtest.tm.bugtracker.advanceddomain;

import java.util.Collection;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.tm.bugtracker.definition.RemoteProject;

public class AdvancedProject implements RemoteProject {

	private String id;
	private String name;

	// Beware adding your schemes in this map will not show them in the Anomaly declaration form.
	// To have a visible scheme selector, you must create a {@link Field} with an {@link InputType} configured with the flag {@link InputType#isFieldSchemeSelector()}
	// See documentation of {@link InputType} for further information. You can also look inside avanced bugtrackers plugin like Jira to see exemple of code.
	private MultiValueMap schemes = new MultiValueMap();

	public void setId(String id){
		this.id = id;
	}

	public void setName(String name){
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}


	/**
	 * think of it as a Map<String, Collection<Field>>
	 */
	public MultiMap getSchemes() {
		return schemes;
	}


	/**
	 * think of it as a Map<String, Collection<Field>>
	 */
	public void setSchemes(MultiValueMap schemes) {
		this.schemes = schemes;
	}


	public Collection<Field> getFieldScheme(String schemeName){
		return (Collection<Field>)schemes.get(schemeName);
	}

}
