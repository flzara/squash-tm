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
package org.squashtest.tm.domain.search;

import org.squashtest.tm.exception.customfield.WrongCufNumericFormatException;

public class AdvancedSearchNumericRangeFieldModel implements AdvancedSearchFieldModel{

	private AdvancedSearchFieldModelType type = AdvancedSearchFieldModelType.NUMERIC_RANGE;

	private String minValue;

	private String maxValue;

	private boolean ignoreBridge = false;

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	public Double getMaxValueAsDouble() {
		try {
			String stdMaxValue = this.maxValue.replace(",",".");
			return Double.parseDouble(stdMaxValue);
		} catch (NumberFormatException nfe) {
			return Double.POSITIVE_INFINITY;//NOSONAR it's handled by replacing the invalid user input value with a constant.
		}
	}

	public Double getMinValueAsDouble() {
		try {
			String stdMinValue = this.minValue.replace(",",".");
			return Double.parseDouble(stdMinValue);
		} catch (NumberFormatException nfe) {
			return Double.NEGATIVE_INFINITY;//NOSONAR it's handled by replacing the invalid user input value with a constant.
		}
	}

	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}



	@Override
	public AdvancedSearchFieldModelType getType() {
		return this.type;
	}

	@Override
	public boolean isIgnoreBridge() {
		return this.ignoreBridge;
	}
}
