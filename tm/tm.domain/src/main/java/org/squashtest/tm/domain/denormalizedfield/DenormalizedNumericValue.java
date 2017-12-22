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

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.NumericCustomFieldValue;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * Created by jthebault on 25/07/2016.
 */
@Entity
@DiscriminatorValue("NUM")
public class DenormalizedNumericValue extends DenormalizedFieldValue {

	private BigDecimal numericValue;

	public DenormalizedNumericValue() {
		super();
	}

	public DenormalizedNumericValue(CustomFieldValue customFieldValue, Long denormalizedFieldHolderId,
									DenormalizedFieldHolderType denormalizedFieldHolderType) {

		this.customFieldValue = customFieldValue;
		CustomField cuf = customFieldValue.getCustomField();
		this.code = cuf.getCode();
		this.inputType = cuf.getInputType();
		this.label = cuf.getLabel();
		this.value = customFieldValue.getValue();
		setNumericValue();
		this.position = customFieldValue.getBinding().getPosition();
		this.renderingLocations = customFieldValue.getBinding().copyRenderingLocations();
		this.denormalizedFieldHolderId = denormalizedFieldHolderId;
		this.denormalizedFieldHolderType = denormalizedFieldHolderType;
	}

	public BigDecimal getNumericValue() {
		return numericValue;
	}

	public void setNumericValue(BigDecimal numericValue) {
		this.numericValue = numericValue;
	}

	public void setNumericValue() {
		if(StringUtils.isBlank(this.value)){
			this.numericValue  = null;
		}else {
			//reformating the "," separator to a "." so whe can handle the two main forms of numeric separators
			String formattedDefaultValue = this.value.replace(",", ".");
			BigDecimal bigDecimal = new BigDecimal(formattedDefaultValue);
			this.numericValue = bigDecimal;
		}
	}
}
