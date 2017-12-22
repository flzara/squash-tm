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
package org.squashtest.tm.service.internal.batchimport;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.BigDecimalValidator;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.service.importer.LogEntry;
import org.squashtest.tm.service.importer.Target;
import org.squashtest.tm.service.internal.dto.NumericCufHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class CustomFieldValidator {

	private static final String TRUE = "TRUE";
	private static final String FALSE = "FALSE";

	private MultiValueMap optionsByListCode = new MultiValueMap();

	public CustomFieldValidator() {
		super();
	}

	/**
	 * check the custom fields for the import mode UPDATE. Empty values are legal.
	 */
	LogTrain checkUpdateCustomFields(Target target, Map<String, String> cufs, Collection<CustomField> definitions) {

		LogTrain train = new LogTrain();

		for (CustomField cuf : definitions) {

			String code = cuf.getCode();
			String value = cufs.get(code);

			CustomFieldError error = checkCustomField(value, cuf);

			if (error != null) {
				String[] errorArgs = {code};
				String errorMessage = error.getErrorMessage();
				String impact = error.getUpdateImpact();
				CustomFieldError.updateValue(cufs, cuf, value, impact);
				LogEntry entry = LogEntry.warning()
					.forTarget(target)
					.withMessage(errorMessage, errorArgs)
					.withImpact(impact).build();
				train.addEntry(entry);
			}
		}

		return train;

	}

	/**
	 * a strong check does not tolerate empty values for custom fields when the said custom field is mandatory. Useful
	 * for modes create or replace
	 *
	 * @param cufs
	 *            a map of (CODE, VALUE) for each custom fields read in the file
	 * @param definitions
	 *            the list of custom fields to check against
	 */
	LogTrain checkCreateCustomFields(Target target, Map<String, String> cufs, Collection<CustomField> definitions) {

		LogTrain train = new LogTrain();

		for (CustomField cuf : definitions) {

			String code = cuf.getCode();
			String value = cufs.get(code);
			CustomFieldError error = checkCustomField(value, cuf);
			if (error != null) {
				String[] errorArgs = {code};
				String errorMessage = error.getErrorMessage();
				String impact = error.getCreateImpact();
				CustomFieldError.updateValue(cufs, cuf, value, impact);
				LogEntry entry = LogEntry.warning()
					.forTarget(target)
					.withMessage(errorMessage, errorArgs)
					.withImpact(impact).build();
				train.addEntry(entry);
			}
		}

		return train;
	}

	private CustomFieldError checkCustomField(String inputValue, CustomField cuf) {

		CustomFieldError error = null;
		InputType type = cuf.getInputType();
		if (StringUtils.isNotBlank(inputValue)) {

			error = checkForType(inputValue, cuf, type);

		} else if (!cuf.isOptional()) {
			error = CustomFieldError.MANDATORY_CUF;
		}

		return error;
	}

	@SuppressWarnings("unchecked")
	public CustomFieldError checkForType(String inputValue, CustomField cuf, InputType type) {
		CustomFieldError error = null;
		switch (type) {

			case PLAIN_TEXT:
				if (inputValue.length() > CustomFieldValue.MAX_SIZE) {
					error = CustomFieldError.MAX_SIZE;
				}
				break;

			case CHECKBOX:
				if (!(TRUE.equalsIgnoreCase(inputValue) || FALSE.equalsIgnoreCase(inputValue))) {
					error = CustomFieldError.UNPARSABLE_CHECKBOX;
				}
				break;

			case DATE_PICKER:
				// if the weak check is not enough, swap for the string check
				if (!StringUtils.isBlank(inputValue) && !DateUtils.weakCheckIso8601Date(inputValue)) {
					error = CustomFieldError.UNPARSABLE_DATE;

				}
				break;

			case DROPDOWN_LIST:
				// cache the options if needed
				registerOptions(cuf);
				Collection<String> options = optionsByListCode.getCollection(cuf.getCode());
				if (!options.contains(inputValue)) {
					error = CustomFieldError.UNPARSABLE_OPTION;
				}
				break;

			case RICH_TEXT:
				// TODO : some day worry about well formed html and malicious js scripts
				break;

			case TAG:
				// nothing to check
				break;

			case NUMERIC:
				if (StringUtils.isBlank(inputValue)){
					error = CustomFieldError.UNPARSABLE_NUMBER;
				}
				else {
					error = validateNumericCustomField(inputValue);
				}
				break;
			default:
				error = CustomFieldError.UNKNOWN_CUF_TYPE;
				break;
		}
		return error;
	}

	private CustomFieldError validateNumericCustomField(String inputValue) {
		String formattedInputValue = NumericCufHelper.formatInputNumericCufValue(inputValue);
		BigDecimalValidator validator = BigDecimalValidator.getInstance();
		boolean valid = validator.isValid(formattedInputValue);
		if (!valid){
            return CustomFieldError.UNPARSABLE_NUMBER;
        }
		return null;
	}

	private void registerOptions(CustomField cuf) {
		String code = cuf.getCode();
		if (!optionsByListCode.containsKey(cuf.getCode())) {
			List<CustomFieldOption> options = ((SingleSelectField) cuf).getOptions();
			for (CustomFieldOption op : options) {
				optionsByListCode.put(code, op.getLabel());
			}
		}
	}

}
