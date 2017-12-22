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
package org.squashtest.tm.web.internal.controller.customfield;

import org.squashtest.tm.domain.customfield.*;

/**
 * @author Gregory Fouquet
 *
 */
public class NewCustomField extends CustomField {
	private InputType inputType;
	private String[][] options;

	public NewCustomField() {
		super(InputType.PLAIN_TEXT);
	}

	public CustomField createTransientEntity() {
		CustomField res;
		switch (inputType) {
		case DROPDOWN_LIST:
			res = createSingleSelectField();
			break;
		case RICH_TEXT :
			res = createRichTextField();
			break;
		case TAG :
			res = createTag();
			break;
		case NUMERIC:
			res = createNumeric();
			break;
		default:
			res = new CustomField(inputType);
		}
		res.setCode(getCode());
		res.setLabel(getLabel());
		res.setName(getName());
		res.setOptional(isOptional());
		res.setDefaultValue(getDefaultValue());

		return res;
	}

	private CustomField createNumeric() {
		return new NumericField();
	}

	private CustomField createSingleSelectField() {
		CustomField res;
		SingleSelectField ssf = new SingleSelectField();

		for(String[] option : options) {
			String label = option[0];
			String code = option[1];
			ssf.addOption(new CustomFieldOption(label, code) );
		}

		res = ssf;
		return res;
	}

	private CustomField createRichTextField(){
		return new RichTextField();
	}

	private CustomField createTag(){
		return new MultiSelectField();
	}

	/**
	 * @return the inputType
	 */
	@Override
	public InputType getInputType() {
		return inputType;
	}

	/**
	 * @param inputType
	 *            the inputType to set
	 */
	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	/**
	 * @return the options
	 */
	public String[][] getOptions() {
		return options;
	}

	/**
	 * @param options
	 *            the options to set
	 */
	public void setOptions(String[][] options) {
		this.options = options.clone();
	}
}
