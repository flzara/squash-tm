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
package org.squashtest.tm.service.internal.customfield;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.event.ChangeCustomFieldCodeEvent;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.exception.customfield.CodeAlreadyExistsException;
import org.squashtest.tm.exception.customfield.DefaultValueRequiredException;
import org.squashtest.tm.service.customfield.CustomCustomFieldManagerService;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

/**
 * Implementations for (non dynamically generated) custom-field management services.
 *
 * @author mpagnon
 *
 */
@Service("CustomCustomFieldManagerService")
public class CustomCustomFieldManagerServiceImpl implements CustomCustomFieldManagerService {

	@Inject
	private CustomFieldDao customFieldDao;

	@Inject
	private CustomFieldBindingDao customFieldBindingDao;

	@Inject
	private CustomFieldValueDao customFieldValueDao;

	@Inject
	private CustomFieldBindingModificationService customFieldBindingModificationService;

	@Inject
	private ApplicationEventPublisher eventPublisher;

	/**
	 * @see org.squashtest.tm.service.customfield.CustomFieldFinderService#findSortedCustomFields(Pageable)
	 */
	@Override
	public Page<CustomField> findSortedCustomFields(Pageable pageable) {
		return customFieldDao.findAll(pageable);
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#deleteCustomField(long)
	 */
	@Override
	public void deleteCustomField(long customFieldId) {
		CustomField customField = customFieldDao.findById(customFieldId);
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllByCustomFieldIdOrderByPositionAsc(customFieldId);
		List<Long> bindingIds = new ArrayList<>();
		for (CustomFieldBinding binding : bindings) {
			bindingIds.add(binding.getId());
		}
		if (!bindingIds.isEmpty()) {
			customFieldBindingModificationService.removeCustomFieldBindings(bindingIds);
		}
		customFieldDao.delete(customField);
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#deleteCustomField(long)
	 */
	@Override
	public void deleteCustomField(List<Long> customFieldIds) {
		for (Long id : customFieldIds) {
			deleteCustomField(id);
		}
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#persist(org.squashtest.tm.domain.customfield.CustomField)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void persist(CustomField newCustomField) {
		checkDuplicateName(newCustomField);
		checkDuplicateCode(newCustomField);
		customFieldDao.save(newCustomField);

	}

	private void checkDuplicateCode(CustomField newCustomField) {
		CustomField codeDuplicate = customFieldDao.findByCode(newCustomField.getCode());
		if (codeDuplicate != null) {
			throw new CodeAlreadyExistsException(null, newCustomField.getCode(), CustomField.class);
		}
	}

	private void checkDuplicateName(CustomField newCustomField) {
		CustomField nameDuplicate = customFieldDao.findByName(newCustomField.getName());
		if (nameDuplicate != null) {
			throw new NameAlreadyInUseException("CustomField", newCustomField.getName());
		}
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#changeName(long, String)
	 */
	@Override
	public void changeName(long customFieldId, String newName) {
		String trimedNewName = newName.trim();
		CustomField customField = customFieldDao.findById(customFieldId);
		String oldName = customField.getName();
		if (customFieldDao.findByName(trimedNewName) != null) {
			throw new DuplicateNameException(oldName, trimedNewName);
		} else {
			customField.setName(trimedNewName);
		}

	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#changeOptional(Long, Boolean)
	 */
	@Override
	public void changeOptional(Long customFieldId, Boolean optional) {
		CustomField customField = customFieldDao.findById(customFieldId);
		if (!optional) {
			checkDefaultValueExists(customField);
			addDefaultValueToCustomFields(customFieldId, customField.getDefaultValue());
		}
		customField.setOptional(optional);
	}


	private void checkDefaultValueExists(CustomField customField) {
		if (customField.getDefaultValue() == null || customField.getDefaultValue().isEmpty()) {
			throw new DefaultValueRequiredException();
		}
	}

	private void addDefaultValueToCustomFields(Long customFieldId, String defaulfValue) {
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllByCustomFieldIdOrderByPositionAsc(customFieldId);
		for (CustomFieldBinding binding : bindings) {
			List<CustomFieldValue> values = customFieldValueDao.findAllCustomValuesOfBinding(binding.getId());
			for (CustomFieldValue value : values) {
				if (value.getValue() == null || value.getValue().isEmpty()) {
					value.setValue(defaulfValue);
				}
			}
		}
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#changeOptionLabel(Long, String,
	 *      String)
	 */
	@Override
	public void changeOptionLabel(Long customFieldId, String optionLabel, String newLabel) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.changeOptionLabel(optionLabel, newLabel);
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#changeOptionCode(Long, String,
	 *      String)
	 */
	@Override
	public void changeOptionCode(long customFieldId, String optionLabel, String newCode) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.changeOptionCode(optionLabel, newCode);
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#addOption(Long, CustomFieldOption)
	 */
	@Override
	public void addOption(Long customFieldId, CustomFieldOption option) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.addOption(option);

	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomFieldFinderService#findSingleSelectFieldById(Long)
	 */
	@Override
	public SingleSelectField findSingleSelectFieldById(Long customFieldId) {
		return customFieldDao.findSingleSelectFieldById(customFieldId);
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#removeOption(long, String)
	 */
	@Override
	public void removeOption(long customFieldId, String optionLabel) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.removeOption(optionLabel);
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#changeOptionsPositions(long, int,
	 *      List)
	 */
	@Override
	public void changeOptionsPositions(long customFieldId, int newIndex, List<String> optionsLabels) {
		SingleSelectField customField = customFieldDao.findSingleSelectFieldById(customFieldId);
		customField.moveOptions(newIndex, optionsLabels);
	}

	/**
	 * @see org.squashtest.tm.service.customfield.CustomCustomFieldManagerService#changeCode(long, String)
	 */
	@Override
	public void changeCode(long customFieldId, String code) {
		CustomField field = customFieldDao.findById(customFieldId);
		checkDuplicateCode(field, code);
		String oldCode = field.getCode();
		field.setCode(code);
		eventPublisher.publishEvent(new ChangeCustomFieldCodeEvent(new String[]{oldCode, code}));

	}

	private void checkDuplicateCode(CustomField field, String newCode) {
		if (StringUtils.equals(field.getCode(), newCode)) {
			return;
		}

		if (customFieldDao.findByCode(newCode) != null) {
			throw new CodeAlreadyExistsException(field.getCode(), newCode, CustomField.class);
		}
	}

	@Override
	public List<String> getAvailableTagsForEntity(String boundEntityType, List<Long> projectIds) {

		return customFieldValueDao.findAllAvailableTagForEntityInProjects(BindableEntity.valueOf(boundEntityType), projectIds);
	}


}
