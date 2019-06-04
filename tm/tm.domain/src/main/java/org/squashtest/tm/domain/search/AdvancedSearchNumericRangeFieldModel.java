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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.NumberFormat;
import java.util.Locale;

public class AdvancedSearchNumericRangeFieldModel implements AdvancedSearchFieldModel{

	private AdvancedSearchFieldModelType type;

	private String minValue;
	private String maxValue;


	public AdvancedSearchNumericRangeFieldModel() {
		type = AdvancedSearchFieldModelType.NUMERIC_RANGE;
	}

	public AdvancedSearchNumericRangeFieldModel(AdvancedSearchFieldModelType type) {
		this.type = type;
	}

	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	@JsonIgnore
	public double getLocaleAgnosticMinValue(){
		try{
			return getLocaleAgnosticValue(minValue);
		}
		catch(NumberFormatException nfe){
			return Double.NEGATIVE_INFINITY; //NOSONAR it's handled by replacing the invalid user input value with a constant.
		}
	}

	@JsonIgnore
	public double getLocaleAgnosticMaxValue(){
		try{
			return getLocaleAgnosticValue(maxValue);
		}
		catch(NumberFormatException nfe){
			return Double.POSITIVE_INFINITY; //NOSONAR it's handled by replacing the invalid user input value with a constant.
		}
	}

	@JsonIgnore
	public boolean hasMinValue(){
		return ! StringUtils.isBlank(minValue);
	}

	@JsonIgnore
	public boolean hasMaxValue(){
		return ! StringUtils.isBlank(maxValue);
	}


	private double getLocaleAgnosticValue(String opinionatedValue){
		Locale locale = LocaleContextHolder.getLocale();
		NumberFormat format = NumberFormat.getInstance(locale);
		double asDouble = Double.parseDouble(opinionatedValue);
		return asDouble;
	}


	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public AdvancedSearchFieldModelType getType() {
		return this.type;
	}

	@Override
	public boolean isSet() {
		return hasMaxValue() || hasMinValue();
	}
}
