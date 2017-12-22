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

import static org.squashtest.tm.api.report.form.InputType.INPUTS_GROUP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.CheckboxesGroup;
import org.squashtest.tm.api.report.form.DropdownList;
import org.squashtest.tm.api.report.form.Input;
import org.squashtest.tm.api.report.form.InputType;
import org.squashtest.tm.api.report.form.InputsGroup;
import org.squashtest.tm.api.report.form.OptionInput;
import org.squashtest.tm.api.report.form.RadioButtonsGroup;
import org.squashtest.tm.domain.project.Project;

/**
 * Converts the post data of a "concise" report form into a Map of Criteria which can be given to a Report.
 * <p/>
 * The former FormToCriteriaConverter was "verbose" ie it was able to reach the max allowed sise of an http request
 * (issue #3762)
 *
 * @author Gregory Fouquet
 */
public class ConciseFormToCriteriaConverter {
	/*
	 * consts for concise form
	 */
	private static final String CON_VAL = "val";
	private static final String CON_TYPE = "type";
	/*
	 * consts for expanded form
	 */
	private static final String EXP_TYPE = "type";
	private static final String EXP_VALUE = "value";
	private static final String EXP_SEL = "selected";

	private final FormToCriteriaConverter delegate = new FormToCriteriaConverter();
	private final Map<String, Input> flattenedInputByName = new HashMap<>();
	private final List<Project> projects;

	public ConciseFormToCriteriaConverter(@NotNull Report report, @NotNull List<Project> projects) {
		super();
		this.projects = projects;
		Collection<Input> flattenedInputs = flattenInputs(Arrays.asList(report.getForm()));
		for (Input input : flattenedInputs) {
			flattenedInputByName.put(input.getName(), input);
		}
	}

	public Map<String, Criteria> convert(Map<String, Object> conciseForm) {
		return delegate.convert(expand(conciseForm));
	}

	private Map<String, Object> expand(Map<String, Object> conciseForm) {
		Map<String, Object> expandedForm = expandedForm();
		populateExpandedForm(conciseForm, expandedForm);

		return expandedForm;
	}

	private void populateExpandedForm(Map<String, Object> conciseForm, Map<String, Object> expandedForm) {
		for (Entry<String, Object> conciseInput : conciseForm.entrySet()) {
			populateExpandedInput(conciseInput.getKey(), conciseInput.getValue(), expandedForm);
		}

	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void populateExpandedInput(String inputName, Object inputValue, Map<String, Object> expandedForm) {
		Map concise = (Map) inputValue;
		InputType type = InputType.valueOf((String) concise.get(CON_TYPE));

		Object expanded = null;

		switch (type) { // NOSONAR
			case TEXT:
			case PASSWORD:
			case DATE: 
				expanded = expendedSingleInput(concise);
				break;
		
			case CHECKBOX:
				expanded = expandedCheckbox(concise);
				break;
			
			
			case RADIO_BUTTONS_GROUP:
				expanded = expandedRadioButtonGroup(inputName, concise);
				break;

			
			case DROPDOWN_LIST:
				expanded = expandedDropdownList(inputName, concise);
				break;

			
			case CHECKBOXES_GROUP:
				expanded = expandedCheckboxesGroup(inputName, concise);
				break;


			case PROJECT_PICKER:
				expanded = expandedProjectPicker(concise);
				break;

			
			case TREE_PICKER:
				Collection<Map> selNodes = (Collection<Map>) concise.get(CON_VAL);
				if (selNodes.isEmpty()) {
					return;
				}
				expanded = expendedTreePicker(concise);
				break;


			case MILESTONE_PICKER:
			case TAG_PICKER:
				expanded = expandedPickerList(concise);
				break;

			
			default:
				// NOOP
				break;
		}

		expandedForm.put(inputName, expanded);
	}
	
	private List<Object> expandedPickerList(Map concise) {
		Collection values = (Collection) concise.get(CON_VAL);

		List<Object> expandedModel = new ArrayList<>();

		Map<String, Object> mMap = new HashMap<>();
		mMap.put(EXP_TYPE, concise.get(CON_TYPE));
		mMap.put(EXP_VALUE, values);

		expandedModel.add(mMap);
		
		return expandedModel;
	}

	private List expendedTreePicker(Map concise) {

		Collection<Map> selNodes = (Collection<Map>) concise.get(CON_VAL);
		
		List exp = new ArrayList();

		for (Map node : selNodes) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, node.get("resid"));
			expOpt.put("nodeType", node.get("restype"));

			exp.add(expOpt);
		}
		return exp;
	}

	private Map expendedSingleInput(Map concise) {
		Map exp = new HashMap();
		exp.put(EXP_TYPE, concise.get(CON_TYPE));
		exp.put(EXP_VALUE, concise.get(CON_VAL));
		return exp;
	}

	@SuppressWarnings("unchecked")
	private Map expandedCheckbox(Map concise) {
		Map exp = new HashMap();
		exp.put(EXP_TYPE, concise.get(CON_TYPE));
		exp.put(EXP_SEL, concise.get(CON_VAL));
		exp.put(EXP_VALUE, "");
		return exp;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private List expandedProjectPicker(Map concise) {
		List exp = new ArrayList();
		Collection selVals = (Collection) concise.get(CON_VAL);

		for (Project p : projects) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, String.valueOf(p.getId()));
			expOpt.put(EXP_SEL, selVals.contains(String.valueOf(p.getId())));

			exp.add(expOpt);
		}
		return exp;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private List expandedRadioButtonGroup(String inputName, Map concise) {
		RadioButtonsGroup reportInput = (RadioButtonsGroup) flattenedInputByName.get(inputName);
		List exp = new ArrayList();
		Object selVal = concise.get(CON_VAL);

		for (OptionInput opt : reportInput.getOptions()) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, opt.getValue());
			expOpt.put(EXP_SEL, selVal.equals(opt.getValue()));

			exp.add(expOpt);
		}
		return exp;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private List expandedDropdownList(String inputName, Map concise) {
		DropdownList reportInput = (DropdownList) flattenedInputByName.get(inputName);
		List exp = new ArrayList();
		Object selVal = concise.get(CON_VAL);

		for (OptionInput opt : reportInput.getOptions()) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, opt.getValue());
			expOpt.put(EXP_SEL, selVal.equals(opt.getValue()));

			exp.add(expOpt);
		}
		return exp;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private List expandedCheckboxesGroup(String inputName, Map concise) {
		CheckboxesGroup reportInput = (CheckboxesGroup) flattenedInputByName.get(inputName);
		List exp = new ArrayList();
		Collection selVals = (Collection) concise.get(CON_VAL);

		for (OptionInput opt : reportInput.getOptions()) {
			Map expOpt = new HashMap();
			expOpt.put(EXP_TYPE, concise.get(CON_TYPE));
			expOpt.put(EXP_VALUE, opt.getValue());
			expOpt.put(EXP_SEL, selVals.contains(opt.getValue()));

			exp.add(expOpt);
		}
		return exp;
	}


	private Map<String, Object> expandedForm() {
		return new HashMap<>();
	}

	private Collection<Input> flattenInputs(List<Input> inputs) {
		Collection<Input> res = new ArrayList<>();

		for (Input input : inputs) {
			if (INPUTS_GROUP == input.getType()) {
				res.addAll(flattenInputs(((InputsGroup) input).getInputs()));
			} else {
				res.add(input);
			}
		}

		return res;
	}

}
