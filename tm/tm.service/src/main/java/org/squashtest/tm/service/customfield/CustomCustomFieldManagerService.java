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
package org.squashtest.tm.service.customfield;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

import java.util.List;

import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldOption;
import org.squashtest.tm.domain.customfield.SingleSelectField;
import org.squashtest.tm.exception.customfield.CannotDeleteDefaultOptionException;
import org.squashtest.tm.exception.customfield.DefaultValueRequiredException;
import org.squashtest.tm.exception.customfield.OptionAlreadyExistException;

/**
 * Custom-Field manager services which cannot be dynamically generated.
 * 
 * @author mpagnon
 * 
 */
@Transactional
public interface CustomCustomFieldManagerService {
	/**
	 * Will delete the custom-field entity
	 * 
	 * @param customFieldId
	 *            : the id of the custom field to delete
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void deleteCustomField(long customFieldId);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void deleteCustomField(List<Long> customFieldIds);

	/**
	 * Will persist the given custom field.
	 * 
	 * @param newCustomField
	 *            : the custom field to persist
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void persist(@NotNull CustomField newCustomField);

	/**
	 * Will check if new name is available among all custom fields and, if so, will change the name of the concerned
	 * {@link CustomField}.
	 * 
	 * @param customFieldId
	 *            the id of the concerned {@link CustomField}
	 * @param newName
	 *            the {@link CustomField} potential new name
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeName(long customFieldId, String newName);

	/**
	 * If custom-field becomes mandatory, will check that a default value exist. If so, all necessary CustomFieldValues
	 * will be added, otherwise an exception is thrown.<br>
	 * If custom-field becomes optional the change is done without check of special action.<br>
	 * 
	 * @param customFieldId
	 *            the id of the concerned {@link CustomField}
	 * @param optional
	 *            : <code>true</code> if the custom-field changes to be optional<br>
	 *            <code>false</code> if it changes to be mandatory
	 * @throws DefaultValueRequiredException
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeOptional(Long customFieldId, Boolean optional);

	/**
	 * Will check if the new label is available among all the concerned {@link CustomField}'s {@link CustomFieldOption},
	 * if so, will change the label of the concerned custom-field's option.
	 * 
	 * @throws OptionAlreadyExistException
	 * @param customFieldId
	 *            : the id of the concerned {@link CustomField}
	 * @param optionLabel
	 *            : the current {@link CustomFieldOption}'s label
	 * @param newLabel
	 *            : the potential new label for the concerned custom-field's option
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeOptionLabel(Long customFieldId, String optionLabel, String newLabel);

	/**
	 * Will check if the new code is available among all the concerned {@link CustomField}'s {@link CustomFieldOption},
	 * if so, will change the code of the concerned custom-field's option.
	 * 
	 * @throws CodeAlreadyExistException
	 * @param customFieldId
	 *            : the id of the concerned {@link CustomField}
	 * @param optionLabel
	 *            : the {@link CustomFieldOption}'s label
	 * @param newCode
	 *            : the potential new code for the concerned custom-field's option
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeOptionCode(long customFieldId, String optionLabel, String newCode);

	/**
	 * Will check if the new option's label is available among all the concerned {@link CustomField}'s
	 * {@link CustomFieldOption}, check also if the code is available,if so, will add the new option at the bottom of the list.
	 * 
	 * @throws OptionAlreadyExistException
	 * @param customFieldId
	 *            : the id of the concerned {@link CustomField}
	 * @param option : the new {@link CustomFieldOption}
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void addOption(Long customFieldId, CustomFieldOption option);

	/**
	 * Will remove the from the custom-field's option list. If the option to remove is the default one, will throw a
	 * {@link CannotDeleteDefaultOptionException}
	 * 
	 * @param customFieldId
	 *            : the id of the concerned {@link SingleSelectField}
	 * @param optionLabel
	 *            : the label of the {@link CustomFieldOption} to remove.
	 * @throws CannotDeleteDefaultOptionException
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void removeOption(long customFieldId, String optionLabel);

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
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeOptionsPositions(long customFieldId, int newIndex, List<String> optionsLabels);

	/**
	 * Will find all custom fields available and return them ordered according to the given parameters.
	 * 
	 * @param filter
	 *            the {@link Page} that holds order and paging params.
	 * @return the filtered collection of all custom field available in squash
	 */
	Page<CustomField> findSortedCustomFields(Pageable filter);

	/**
	 * Will find the {@link SingleSelectField} of the given id
	 * 
	 * @param customFieldId
	 *            the id of the {@link SingleSelectField}
	 * @return the {@link SingleSelectField} or <code>null</code>
	 */
	SingleSelectField findSingleSelectFieldById(Long customFieldId);

	/**
	 * Will change the code of the custom field after having checked that : the code is unique among all custom fields,
	 * and that the code contains only letters (upper and lower cases), numbers or under-scores.
	 * 
	 * @param customFieldId
	 *            : the id of the concerned {@link CustomField}
	 * @param code
	 *            : the new code
	 */
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeCode(long customFieldId, String code);

	List<String> getAvailableTagsForEntity(String boundEntityType, List<Long> projectIds);

}
