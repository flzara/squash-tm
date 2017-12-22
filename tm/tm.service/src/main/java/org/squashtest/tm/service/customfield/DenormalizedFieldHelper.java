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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager;

/**
 * This version exists for the {@link DenormalizedFieldHolder}, which are structurally equivalent to
 * {@link BoundEntity} but with a different class domain.
 *
 * For the API see {@link CustomFieldHelper}, except that we'll deal with {@link DenormalizedFieldValue}
 * instead of {@link CustomFieldValue}.
 *
 * Note that for now the only adding strategy is "includeAllCustomFields" .
 *
 * @author bsiri
 *
 * @param <X>
 */
public class DenormalizedFieldHelper<X extends DenormalizedFieldHolder>{

	private DenormalizedFieldValueManager dcufFinder;


	// ************ attributes ******************

	private Collection<RenderingLocation> locations;
	private List<CustomField> customFields;

	private List<DenormalizedFieldValue> values;
	private final Collection<X> entities;

	// ************* code ************************

	public DenormalizedFieldHelper(X entity) {
		this.entities = new ArrayList<>();
		this.entities.add(entity);
	}

	public DenormalizedFieldHelper(Collection<X> entities) {
		this.entities = entities;
	}

	public void setDenormalizedFieldValueFinder(DenormalizedFieldValueManager finder){
		this.dcufFinder = finder;
	}

	// ************* API section ****************************

	public DenormalizedFieldHelper<X> setRenderingLocations(RenderingLocation...locations){
		this.locations = Arrays.asList(locations);
		return this;
	}

	public DenormalizedFieldHelper<X> setRenderingLocations(Collection<RenderingLocation> locations){
		this.locations = locations;
		return this;
	}


	public List<CustomField> getCustomFieldConfiguration(){
		if (!isInited()) {
			init();
		}

		return customFields;
	}

	public List<DenormalizedFieldValue> getDenormalizedFieldValues(){
		if (!isInited()) {
			init();
		}

		return values;

	}

	// ******************* private stuffs ******************


	protected void init(){

		if (! entities.isEmpty()){

			//get the values
			findValues();

			//extract the custom fields according to the adding strategy
			extractCustomFields();

		}
		else{
			values = Collections.emptyList();
			customFields = Collections.emptyList();
		}

	}


	private void findValues(){
		values = dcufFinder.findAllForEntities((Collection<DenormalizedFieldHolder>)entities, locations);
	}


	// remember that for now we only care of AbstractCustomFieldHelper.CustomFieldDefinitionStrategy.UNION
	// so we include every custom fields that exist at least in one step.
	private void extractCustomFields(){

		Map<String, CustomField> cfMap = new HashMap<>();

		CustomField customField;
		for (DenormalizedFieldValue dfv : values){
			if ( cfMap.get(dfv.getCode()) == null){
				customField = new CustomField(dfv.getInputType());
				customField.setCode(dfv.getCode());
				customField.setLabel(dfv.getLabel());
				cfMap.put(customField.getCode(), customField);
			}
		}

		customFields = new ArrayList(cfMap.values());
	}

	private boolean isInited() {
		return customFields != null;
	}

}
