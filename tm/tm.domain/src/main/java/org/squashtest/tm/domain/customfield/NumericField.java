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
import org.hibernate.annotations.Type;
import org.squashtest.tm.exception.customfield.WrongCufNumericFormatException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("NUM")
public class NumericField extends CustomField {

	private BigDecimal numericDefaultValue;

	public NumericField() {
		super(InputType.NUMERIC);
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		if (StringUtils.isBlank(defaultValue)){
			this.defaultValue  = defaultValue;
			this.numericDefaultValue = null;
		} else {
			try {
				//reformating the "," separator to a "." so whe can handle the two main forms of numeric separators
				String formattedDefaultValue = defaultValue.replace(",",".");
				this.numericDefaultValue = new BigDecimal(formattedDefaultValue);
				//we also persist the value as a string, some operations like export will be a lot easier
				this.defaultValue = this.numericDefaultValue.toString();
			} catch (NumberFormatException nfe) {
				throw new WrongCufNumericFormatException(nfe);
			}
		}
	}

	@Override
	public String getDefaultValue() {
		return defaultValue  != null ? defaultValue  : "";
	}

}
