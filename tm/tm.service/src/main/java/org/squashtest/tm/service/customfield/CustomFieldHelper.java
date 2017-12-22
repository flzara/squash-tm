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
package org.squashtest.tm.service.customfield;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;

public interface CustomFieldHelper<X extends BoundEntity> {

	// ***************** builder API ***************
	
	CustomFieldHelper<X> setRenderingLocations(RenderingLocation... locations);

	CustomFieldHelper<X> setRenderingLocations(Collection<RenderingLocation> locations);

	/**
	 * tells the helper to retain only the custom fields that are common to all the entities (in case they come from mixed projects, or are 
	 * of mixed concrete classes)
	 * 
	 * @return this object
	 */
	CustomFieldHelper<X> restrictToCommonFields();

	/**
	 * tells the helper to include every custom fields it finds.
	 * 
	 * @return
	 */
	CustomFieldHelper<X> includeAllCustomFields();

	
	// *************** output API *******************
	
	/**
	 * sorted by position, filtered by location.
	 * 
	 * @return
	 */
	List<CustomField> getCustomFieldConfiguration();

	List<CustomFieldValue> getCustomFieldValues();

}