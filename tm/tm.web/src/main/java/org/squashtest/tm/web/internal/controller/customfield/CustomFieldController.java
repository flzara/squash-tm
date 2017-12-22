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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.core.foundation.collection.SinglePageCollectionHolder;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.exception.DomainException;
import org.squashtest.tm.service.customfield.CustomFieldManagerService;
import org.squashtest.tm.service.internal.dto.NumericCufHelper;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.helper.JEditablePostParams;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

/**
 * Controller for the Custom Fields resources.
 *
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping("/custom-fields")
public class CustomFieldController {


	private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldController.class);

	private static final String CUSTOM_FIELD = "customField";
	private static final String NUMERIC_CUSTOM_FIELD_VALUE = "numericCustomFieldValue";

	@Inject
	private CustomFieldManagerService customFieldManager;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private MessageSource messageSource;

	@ModelAttribute("customFieldOptionsPageSize")
	public long populateCustomFieldsPageSize() {
		return Pagings.DEFAULT_PAGING.getPageSize();
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public void createNew(@RequestBody NewCustomField field) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Will create custom field {}", ToStringBuilder.reflectionToString(field));
		}
		customFieldManager.persist(field.createTransientEntity());
	}

	/**
	 * Shows the custom field modification page.
	 *
	 * @param customFieldId
	 *            the id of the custom field to show
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/{customFieldId}", method = RequestMethod.GET)
	public String showCustomFieldModificationPage(@PathVariable Long customFieldId, Model model) {
		CustomField customField = customFieldManager.findById(customFieldId);

		switch (customField.getInputType()){
			case DROPDOWN_LIST:
				SingleSelectField cuf = customFieldManager.findSingleSelectFieldById(customFieldId);
				model.addAttribute(CUSTOM_FIELD, cuf);
				break;
			case NUMERIC:
				model.addAttribute(CUSTOM_FIELD, customField);
				model.addAttribute(NUMERIC_CUSTOM_FIELD_VALUE, NumericCufHelper.formatOutputNumericCufValue(customField.getDefaultValue()));
				break;
			default:
				model.addAttribute(CUSTOM_FIELD, customField);
		}

		return "custom-field-modification.html";
	}

	@RequestMapping(value = "/name/{name}", params = "id")
	@ResponseBody
	public Object getIdByName(@PathVariable String name) {
		CustomField field = customFieldManager.findByName(name);

		if (field != null) {
			Map<String, Long> res = new HashMap<>(1);
			res.put("id", field.getId());
			return res;
		} else {
			return null;
		}
	}

	/**
	 * Changes the label of the concerned custom field
	 *
	 * @param customFieldId
	 *            the id of the concerned custom field
	 * @param label
	 *            the new label
	 * @return
	 */
	@RequestMapping(value = "/{customFieldId}", method = RequestMethod.POST, params = { "id=cuf-label", JEditablePostParams.VALUE }, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeLabel(@PathVariable long customFieldId, @RequestParam(JEditablePostParams.VALUE) String label) {
		customFieldManager.changeLabel(customFieldId, label);
		return HtmlUtils.htmlEscape(label);
	}

	/**
	 * Changes the code of the concerned custom field
	 *
	 * @param customFieldId
	 *            the id of the concerned custom field
	 * @param code
	 *            the new code
	 * @return
	 */
	@RequestMapping(value = "/{customFieldId}", method = RequestMethod.POST, params = { "id=cuf-code", JEditablePostParams.VALUE })
	@ResponseBody
	public String changeCode(@PathVariable long customFieldId, @RequestParam(JEditablePostParams.VALUE) String code) {
		customFieldManager.changeCode(customFieldId, code);
		return code;
	}

	/**
	 * Changes the name of the concerned custom field
	 *
	 * @param customFieldId
	 *            the id of the concerned custom field
	 * @param name
	 *            the new name
	 * @return
	 */
	@RequestMapping(value = "/{customFieldId}/name", method = RequestMethod.POST, params = { JEditablePostParams.VALUE })
	@ResponseBody
	public Object changeName(@PathVariable long customFieldId, @RequestParam(JEditablePostParams.VALUE) String name) {
		customFieldManager.changeName(customFieldId, name);
		return new RenameModel(name);
	}

	/**
	 * Changes the whether the custom-field is optional or not.
	 *
	 * @param customFieldId
	 *            the id of the concerned custom field
	 * @param optional
	 *            : true if the custom field is optional
	 * @return
	 */
	@RequestMapping(value = "/{customFieldId}/optional", method = RequestMethod.POST, params = { JEditablePostParams.VALUE })
	@ResponseBody
	public boolean changeOptional(@PathVariable long customFieldId, @RequestParam(JEditablePostParams.VALUE) Boolean optional) {
		customFieldManager.changeOptional(customFieldId, optional);
		return optional;
	}

	/**
	 * Changes the default value of the concerned custom-field
	 *
	 * @param customFieldId
	 *            : the id of concerned custom-field
	 * @param defaultValue
	 *            : the new default-value for the custom-field
	 * @param locale
	 *            : the browser's locale
	 *
	 * @return defaultValue
	 */
	@RequestMapping(value = "/{customFieldId}", method = RequestMethod.POST, params = { "id=cuf-default-value", JEditablePostParams.VALUE })
	@ResponseBody
	public String changeDefaultValueJedit(@PathVariable long customFieldId, @RequestParam(JEditablePostParams.VALUE) String defaultValue,
			Locale locale) {
		customFieldManager.changeDefaultValue(customFieldId, defaultValue);
		CustomField customField = customFieldManager.findById(customFieldId);
		String toReturn = defaultValue;
		if (customField.getInputType() == InputType.CHECKBOX) {
			toReturn = messageSource.getMessage("label." + defaultValue, null, locale);
		}
		return toReturn;
	}

	/**
	 * Changes the default value of the concerned custom-field
	 *
	 * @param customFieldId
	 *            : the id of concerned custom-field
	 * @param defaultValue
	 *            : the new default-value for the custom-field
	 */
	@RequestMapping(value = "/{customFieldId}/defaultValue", method = RequestMethod.POST, params = { JEditablePostParams.VALUE })
	@ResponseBody
	public String changeDefaultValue(@PathVariable long customFieldId, @RequestParam(JEditablePostParams.VALUE) String defaultValue) {
		customFieldManager.changeDefaultValue(customFieldId, defaultValue);
		return defaultValue;
	}

	/**
	 * Changes the label of the concerned custom-field's option
	 *
	 * @param customFieldId
	 *            : the id of the concerned custom-field
	 * @param optionLabel
	 *            : the label of the concerned custom-field's option
	 * @param newLabel
	 *            : the new label for the concerned custom-field's option
	 * @return
	 */
	@RequestMapping(value = "/{customFieldId}/options/{optionLabel}/label", method = RequestMethod.POST, params = { JEditablePostParams.VALUE })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void changeOptionLabel(@PathVariable long customFieldId, @PathVariable String optionLabel,
			@RequestParam(JEditablePostParams.VALUE) String newLabel) {
		try {
			customFieldManager.changeOptionLabel(customFieldId, optionLabel, newLabel);
		} catch (DomainException e) {
			e.setObjectName("rename-cuf-option");
			throw e;
		}
	}

	/**
	 * Changes the code of the concerned custom-field's option
	 *
	 * @param customFieldId
	 *            : the id of the concerned custom-field
	 * @param optionLabel
	 *            : the label of the concerned custom-field's option
	 * @param newCode
	 *            : the new code for the concerned custom-field's option
	 * @return
	 */
	@RequestMapping(value = "/{customFieldId}/options/{optionLabel}/code", method = RequestMethod.POST, params = { JEditablePostParams.VALUE })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void changeOptionCode(@PathVariable long customFieldId, @PathVariable String optionLabel,
			@RequestParam(JEditablePostParams.VALUE) String newCode) {
		try {
			customFieldManager.changeOptionCode(customFieldId, optionLabel, newCode);
		} catch (DomainException e) {
			e.setObjectName("change-cuf-option");
			throw e;
		}
	}

	/**
	 * Adds an option to the concerned custom-field
	 *
	 * @param customFieldId
	 *            : the id of the concerned custom-field
	 * @param option
	 *            : the new option
	 */
	@RequestMapping(value = "/{customFieldId}/options/new", method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void addOption(@PathVariable long customFieldId,
			@Valid @ModelAttribute("new-cuf-option") CustomFieldOption option) {
		try {
			customFieldManager.addOption(customFieldId, option);
		} catch (DomainException e) {
			e.setObjectName("new-cuf-option");
			throw e;
		}
	}

	/**
	 * Remove a customField's option
	 *
	 * @param customFieldId
	 *            : the id of the concerned custom-field
	 * @param optionLabel
	 *            : the label of the option to remove
	 */
	@RequestMapping(value = "/{customFieldId}/options/{optionLabel}", method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void removeOption(@PathVariable long customFieldId, @PathVariable String optionLabel) {
		customFieldManager.removeOption(customFieldId, optionLabel);
	}

	/**
	 * Return the DataTableModel to display the table of all custom field's option.
	 *
	 * @param customFieldId
	 *            : the id of the concerned custom field
	 * @param params
	 *            the {@link DataTableDrawParameters} for the custom field's options table
	 * @return the {@link DataTableModel} with organized {@link CustomFieldOption} infos.
	 */
	@RequestMapping(value = "/{customFieldId}/options", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getCustomFieldsTableModel(@PathVariable long customFieldId,
			final DataTableDrawParameters params) {
		SingleSelectField customField = customFieldManager.findSingleSelectFieldById(customFieldId);
		List<CustomFieldOption> customFieldOptions = customField.getOptions();
		PagedCollectionHolder<List<CustomFieldOption>> holder = new SinglePageCollectionHolder<>(customFieldOptions);
		return new CustomFieldOptionsDataTableModelHelper(customField).buildDataModel(holder,params.getsEcho());
	}

	/**
	 * Will help to create the {@link DataTableModel} to fill the data-table of custom field's options
	 *
	 */
	private static final class CustomFieldOptionsDataTableModelHelper extends DataTableModelBuilder<CustomFieldOption> {

		private CustomField customField;

		private CustomFieldOptionsDataTableModelHelper(CustomField customField) {
			this.customField = customField;
		}

		@Override
		public Map<String, Object> buildItemData(CustomFieldOption item) {

			Map<String, Object> res = new HashMap<>();
			String checked = " ";
			if (customField.getDefaultValue().equals(item.getLabel())) {
				checked = " checked='checked' ";
			}
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("opt-label", item.getLabel());
			res.put("opt-code", item.getCode());
			res.put("opt-default", "<input type='checkbox' name='default' value='" + item.getLabel() + "'" + checked
					+ "/>");
			res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return res;
		}
	}

	/**
	 * Will change custom field's options positions.
	 *
	 * @param customFieldId
	 *            : the id of the concerned CustomField.
	 * @param newIndex
	 *            : the lowest index for the moved selection
	 * @param optionsLabels
	 *            : the labels of the moved options
	 */
	@RequestMapping(value = "/{customFieldId}/options/positions", method = RequestMethod.POST, params = { "itemIds[]",
	"newIndex" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void changeOptionsPositions(@PathVariable long customFieldId, @RequestParam int newIndex,
			@RequestParam("itemIds[]") List<String> optionsLabels) {
		customFieldManager.changeOptionsPositions(customFieldId, newIndex, optionsLabels);
	}

	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{customFieldIds}", method = RequestMethod.DELETE)
	public
	void deleteCustomField(@PathVariable("customFieldIds") List<Long> customFieldIds) {
		customFieldManager.deleteCustomField(customFieldIds);
	}

	@RequestMapping(value = "/tags/{boundEntity}", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getPossibleTagValues(@PathVariable("boundEntity") String boundEntity){

	List<Long> projectIds = (List<Long>) CollectionUtils.collect(projectFinder.findAllOrderedByName(), new Transformer() {

			@Override
			public Object transform(Object input) {
				return 	((GenericProject) input).getId();
			}
		});

		return customFieldManager.getAvailableTagsForEntity(boundEntity, projectIds);
	}
}
