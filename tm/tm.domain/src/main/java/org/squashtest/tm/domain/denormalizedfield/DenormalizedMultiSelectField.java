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
package org.squashtest.tm.domain.denormalizedfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.CustomFieldValueOption;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.RawValue;

@Entity
@DiscriminatorValue("MFV")
public class DenormalizedMultiSelectField extends DenormalizedFieldValue {

	@ElementCollection
	@CollectionTable(name = "DENORMALIZED_FIELD_OPTION", joinColumns = @JoinColumn(name = "DFV_ID"))
	@OrderColumn(name = "POSITION")
	private Set<CustomFieldOption> options = new HashSet<>();

	@ElementCollection
	@CollectionTable(name = "DENORMALIZED_FIELD_VALUE_OPTION", joinColumns = @JoinColumn(name = "DFV_ID"))
	@OrderColumn(name = "POSITION")
	private List<CustomFieldValueOption> selectedOptions = new ArrayList<>();


	/**
	 * For ORM purposes.
	 */
	protected DenormalizedMultiSelectField() {
		super();

	}

	public DenormalizedMultiSelectField(CustomFieldValue customFieldValue, Long denormalizedFieldHolderId,
			DenormalizedFieldHolderType denormalizedFieldHolderType) {

		super(customFieldValue, denormalizedFieldHolderId, denormalizedFieldHolderType);

		// the super constructor has set the attribute 'value', which has no meaning for this multi valued
		// custom field.
		this.value = null;

		RawValue rawValue = customFieldValue.asRawValue();
		rawValue.setValueFor(this);
	}



	public List<String> getValues(){
		List<String> result = new ArrayList<>(selectedOptions.size());
		for (CustomFieldValueOption option : selectedOptions){
			result.add(option.getLabel());
		}
		return result;
	}

	public void setValues(List<String> values){
		selectedOptions.clear();
		for (String option : values){
			selectedOptions.add(new CustomFieldValueOption(option));
		}
	}

	public Set<CustomFieldOption> getOptions() {
		return Collections.unmodifiableSet(options);
	}

	@Override
	public void accept(DenormalizedFieldVisitor visitor){
		visitor.visit(this);
	}

	@Override
	public String getValue(){
		return selectedOptions.isEmpty() ? "" : StringUtils.join(getValues(), MultiSelectField.SEPARATOR);
	}

	/**
	 * Not the preferred way to set the values of this field,
	 * use adCUFieldValueOption when possible.
	 */
	@Override
	@Deprecated
	public void setValue(String value){
		setValues(Arrays.asList(value.split(MultiSelectField.SEPARATOR_EXPR)));
	}



}
