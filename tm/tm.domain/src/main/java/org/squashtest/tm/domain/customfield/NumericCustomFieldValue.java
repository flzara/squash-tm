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
import org.squashtest.tm.exception.customfield.WrongCufNumericFormatException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * Created by jthebault on 20/07/2016.
 */
@Entity
@DiscriminatorValue("NUM")
public class NumericCustomFieldValue extends CustomFieldValue {

	//Will be used for research and reporting.
	private BigDecimal numericValue;

	@Override
	public void setValue(String value){
		CustomField field = getCustomField();
		BigDecimal numericValue;
		if (field != null && !field.isOptional() && StringUtils.isBlank(value)){
			throw new MandatoryCufException(this);
		}

		if (field != null && field.isOptional() && StringUtils.isBlank(value)){
			this.numericValue  = null;
			this.value = "";
		}
		else {
			try {
				//reformating the "," separator to a "." so whe can handle the two main forms of numeric separators
				String formattedValue = value.replace(",",".");
				this.numericValue  = new BigDecimal(formattedValue);
				//we also persist the value as a string, some operations like export will be a lot easier
				this.value = this.numericValue.toString();
			} catch (NumberFormatException nfe) {
				throw new WrongCufNumericFormatException(nfe);
			}
		}
	}

	@Override
	public String getValue(){
		return this.value;
	}

	@Override
	public CustomFieldValue copy(){
		CustomFieldValue copy = new NumericCustomFieldValue();
		copy.setBinding(getBinding());
		copy.setValue(getValue());
		copy.setCufId(binding.getCustomField().getId());
		return copy;
	}

	@Override
	public void accept(CustomFieldValueVisitor visitor) {
		visitor.visit(this);
	}

	public BigDecimal getNumericValue() {
		return numericValue;
	}

	@Override
	public RawValue asRawValue() {
		return new RawValue(value);
	}
}
