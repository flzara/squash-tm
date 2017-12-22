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
package org.squashtest.tm.domain.customfield;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.exception.customfield.MandatoryCufException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("TAG")
public class TagsValue extends CustomFieldValue implements MultiValuedCustomFieldValue, CustomFieldVisitor {

	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_VALUE_OPTION", joinColumns = @JoinColumn(name = "CFV_ID"))
	@OrderColumn(name = "POSITION")
	private List<CustomFieldValueOption> selectedOptions = new ArrayList<>();

	@Override
	public List<CustomFieldValueOption> getSelectedOptions() {
		return selectedOptions;
	}

	public void addCUFieldValueOption(CustomFieldValueOption cufVO) {
		selectedOptions.add(cufVO);
	}

	public void removeCUFValueOption(CustomFieldValueOption cufVO) {
		selectedOptions.remove(cufVO);
	}


	@Override
	public void setValues(List<String> values) {
		CustomField cuf = doGetCustomField();
		if(cuf != null) {
			/* Issue #6834 */
			if(!cuf.isOptional() && values.isEmpty()) {
				throw new MandatoryCufException(this);
			}

			selectedOptions.clear();
			for (String option : values) {
				selectedOptions.add(new CustomFieldValueOption(option));
			}

			// now update the available options at the custom field level
			getCustomField().accept(this);
			// ^^^ si tout ce qu'on veut faire c'est un downcast, autant downcaster dans la mesure où le type de custom field
			// est obligatoirement MuliValueWhatever. On peut même encapsuler le downcast dans un `@Override MultiWhatever getCustomField()`
			// (types de retour covariants autorisés depuis java 5)
		}
	}

	@Override
	public List<String> getValues() {
		List<String> result = new ArrayList<>(selectedOptions.size());
		for (CustomFieldValueOption option : selectedOptions) {
			result.add(option.getLabel());
		}
		return result;
	}


	@Override
	public String getValue() {
		return selectedOptions.isEmpty() ? "" : StringUtils.join(getValues(), MultiSelectField.SEPARATOR);
	}

	/**
	 * Not the preferred way to set the values of this field,
	 * use adCUFieldValueOption when possible.
	 */
	@Override
	@Deprecated
	public void setValue(String value) {
		setValues(value == null ? Collections.<String>emptyList() : Arrays.asList(value.split(MultiSelectField.SEPARATOR_EXPR)));
	}

	@Override
	public CustomFieldValue copy() {
		TagsValue copy = new TagsValue();
		copy.setBinding(getBinding());
		copy.setCufId(binding.getCustomField().getId());

		for (CustomFieldValueOption option : selectedOptions) {
			copy.addCUFieldValueOption(option.copy());
		}

		return copy;
	}

	@Override
	public RawValue asRawValue() {
		return new RawValue(getValues());
	}

	@Override
	public void setSelectedOptions(List<CustomFieldValueOption> options) {
		this.selectedOptions = options;
	}

	@Override
	public void visit(SingleSelectField selectField) {
		throw new IllegalArgumentException("a TAG custom field value cannot represent a Single Select Field");
	}

	@Override
	public void visit(CustomField standardValue) {
		throw new IllegalArgumentException("a TAG custom field value cannot represent a standard custom field");
	}

	@Override
	public void visit(RichTextField richField) {
		throw new IllegalArgumentException("a TAG custom field value cannot represent a Rich Text field");
	}

	@Override
	public void visit(NumericField numericField) {
		throw new IllegalArgumentException("a TAG custom field value cannot represent a Numeric custom field");
	}


	// should have been called "updateAvailableOptions"
	@Override
	public void visit(MultiSelectField multiselect) {
		for (CustomFieldValueOption option : selectedOptions) {
			multiselect.addOption(option.getLabel());
		}
	}

	@Override
	public void accept(CustomFieldValueVisitor visitor) {
		visitor.visit(this);
	}

}
