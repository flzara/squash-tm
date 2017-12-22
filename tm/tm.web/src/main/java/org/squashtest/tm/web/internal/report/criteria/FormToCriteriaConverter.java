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
package org.squashtest.tm.web.internal.report.criteria;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.InputType;

import static org.squashtest.tm.api.report.form.InputType.*;

/**
 * This class builds a map of {@link Criteria} from the map of objects which was submitted by a report form.
 *
 * @author Gregory Fouquet
 *
 */
public class FormToCriteriaConverter {
	private static final String INPUT_SELECTED = "selected";
	private static final String INPUT_VALUE = "value";
	private static final String INPUT_TYPE = "type";

	private static final Logger LOGGER = LoggerFactory.getLogger(FormToCriteriaConverter.class);

	private final Map<InputType, SimpleEntryConverter> simpleEntryConverterByType;
	private final SimpleEntryConverter simpleEntryDefaultConverter = new GenericSimpleEntryConverter();

	/**
	 * @param simpleEntryConverterByType
	 */
	public FormToCriteriaConverter() {
		super();

		this.simpleEntryConverterByType = new HashMap<>();
		simpleEntryConverterByType.put(DATE, new DateEntryConverter());
		simpleEntryConverterByType.put(CHECKBOX, new CheckboxEntryConverter());
		simpleEntryConverterByType.put(TEXT, simpleEntryDefaultConverter);
		simpleEntryConverterByType.put(PASSWORD, simpleEntryDefaultConverter);
		simpleEntryConverterByType.put(TREE_PICKER, simpleEntryDefaultConverter);
		simpleEntryConverterByType.put(MILESTONE_PICKER, simpleEntryDefaultConverter);
		simpleEntryConverterByType.put(TAG_PICKER, simpleEntryDefaultConverter);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Criteria> convert(Map<String, Object> formValues) {
		HashMap<String, Criteria> res = new HashMap<>();

		for (Map.Entry<String, Object> entry : formValues.entrySet()) {
			String name = entry.getKey();
			Object inputValue = entry.getValue();

			if (inputValue instanceof Collection) {
				Collection<Map<String, Object>> optionValues = (Collection<Map<String, Object>>) inputValue;
				Criteria crit = convertMultiValuedEntry(name, optionValues);
				res.put(name, crit);
			} else if (inputValue instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) inputValue;
				Criteria crit = convertSimpleEntry(name, map);
				res.put(name, crit);

			} else {
				LOGGER.error("Form {} contains non convertible entry {}", formValues, entry);
				throw new FormEntryNotConvertibleException(entry);
			}
		}

		return res;
	}

	/**
	 * @param name
	 * @param multiValued
	 * @return
	 */
	private Criteria convertMultiValuedEntry(String name, Collection<Map<String, Object>> multiValued) {
		InputType inputType = extractInputType(multiValued);
		return convertMultiValuedEntry(name, multiValued, inputType);
	}

	/**
	 * @param name
	 * @param multiValued
	 * @param inputType
	 * @return
	 */
	private Criteria convertMultiValuedEntry(String name, Collection<Map<String, Object>> multiValued,
			InputType inputType) {
		Criteria res;
		switch (inputType) {
		case TREE_PICKER:
			res = createNodeMapCriteria(name, multiValued, inputType);
			break;
		case CHECKBOXES_GROUP:
		case PROJECT_PICKER:
			res = createMultiOptionsCriteria(name, multiValued, inputType);
			break;
		case RADIO_BUTTONS_GROUP:
		case DROPDOWN_LIST:
			res = createSingleOptionCriteria(name, multiValued, inputType);
			break;
		case MILESTONE_PICKER :
			res = createMilestoneMultiCriteria(name, multiValued, inputType);
			break;

		case TAG_PICKER :
			res = createTagCriteria(name, multiValued, inputType);
			break;
		default:
			res = EmptyCriteria.createEmptyCriteria(name, inputType);
		}
		return res;
	}

	private Criteria createTagCriteria(String name, Collection<Map<String, Object>> multiValued, InputType inputType) {

		MultiOptionsCriteria crit = new MultiOptionsCriteria(name, inputType);

		for (Map<String, Object> valueItem : multiValued){
			Collection<String> tags = (Collection) valueItem.get(INPUT_VALUE);
			for (String tag : tags){
				crit.addOption(tag, Boolean.TRUE);
			}
		}

		return crit;
	}

	/**
	 * @param name
	 * @param multiValued
	 * @param inputType
	 * @return
	 */
	private Criteria createNodeMapCriteria(String name, Collection<Map<String, Object>> multiValued,
			InputType inputType) {
		MultiValuesCriteria crit = new MultiValuesCriteria(name, inputType);

		for (Map<String, Object> valueItem : multiValued) {
			String nodeType = (String) valueItem.get("nodeType");
			Object value = valueItem.get(INPUT_VALUE);
			crit.addValue(nodeType, value);
		}
		return crit;
	}

	private Criteria createSingleOptionCriteria(String name, Collection<Map<String, Object>> multiValued,
			InputType inputType) {
		for (Map<String, Object> valueItem : multiValued) {
			Boolean selected = (Boolean) valueItem.get(INPUT_SELECTED);
			if (selected) {
				return new SimpleCriteria<>(name, (String) valueItem.get(INPUT_VALUE), inputType);
			}
		}
		return new EmptyCriteria(name, inputType);
	}

	private Criteria createMultiOptionsCriteria(String name, Collection<Map<String, Object>> multiValued,
			InputType inputType) {

		MultiOptionsCriteria crit = new MultiOptionsCriteria(name, inputType);

		for (Map<String, Object> valueItem : multiValued) {
			Boolean selected = (Boolean) valueItem.get(INPUT_SELECTED);
			Object value =  valueItem.get(INPUT_VALUE);
			crit.addOption(value, selected);
		}
		return crit;
	}

	private Criteria createMilestoneMultiCriteria(String name, Collection<Map<String, Object>> multiValued, InputType inputType){

		MultiOptionsCriteria crit = new MultiOptionsCriteria(name, inputType);

		for (Map<String, Object> valueItem : multiValued){
			Collection<Integer> selectedIds = (Collection) valueItem.get(INPUT_VALUE);
			for (Integer id : selectedIds){
				crit.addOption(id, Boolean.TRUE);
			}
		}

		return crit;
	}

	/**
	 * @param multiValued
	 * @return
	 */
	private InputType extractInputType(Collection<Map<String, Object>> multiValued) {
		String type = null;

		for (Map<String, Object> valueItem : multiValued) {
			if (type == null) {
				type = (String) valueItem.get(INPUT_TYPE);
			} else {
				if (!type.equals(valueItem.get(INPUT_TYPE))) {
					throw new InconsistentMultiValuedEntryException(multiValued);
				}
			}
		}
		return InputType.valueOf(type);
	}

	private Criteria convertSimpleEntry(String name, Map<String, Object> entry) {

		String type = (String) entry.get(INPUT_TYPE);
		InputType inputType = InputType.valueOf(type);
		return simpleEntryConverter(inputType).convertEntry(name, entry, inputType);
	}

	/**
	 * @param type
	 */
	private SimpleEntryConverter simpleEntryConverter(InputType type) {
		SimpleEntryConverter converter = simpleEntryConverterByType.get(type);

		if (converter == null) {
			converter = simpleEntryDefaultConverter;
		}

		return converter;
	}
}
