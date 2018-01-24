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

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldBinding.PositionAwareBindingList;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.event.CreateCustomFieldBindingEvent;
import org.squashtest.tm.event.DeleteCustomFieldBindingEvent;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;

@Service("squashtest.tm.service.CustomFieldBindingService")
@Transactional
public class CustomFieldBindingModificationServiceImpl implements CustomFieldBindingModificationService {

	@Inject
	private CustomFieldDao customFieldDao;

	@Inject
	private CustomFieldBindingDao customFieldBindingDao;

	@Inject
	private PrivateCustomFieldValueService customValueService;

	@Inject
	private GenericProjectDao genericProjectDao;

	@Inject
	private ApplicationEventPublisher eventPublisher;

	private static final Transformer BINDING_ID_COLLECTOR = new Transformer() {
		@Override
		public Object transform(Object input) {
			return ((CustomFieldBinding) input).getId();
		}
	};

	@Override
	@Transactional(readOnly = true)
	public List<CustomField> findAvailableCustomFields() {
		return customFieldDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomField> findAvailableCustomFields(long projectId, BindableEntity entity) {
		return customFieldDao.findAllBindableCustomFields(projectId, entity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomField> findBoundCustomFields(long projectId, BindableEntity entity) {
		return customFieldDao.findAllBoundCustomFields(projectId, entity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomFieldBinding> findCustomFieldsForGenericProject(long projectId) {
		return customFieldBindingDao.findAllForGenericProject(projectId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomFieldBinding> findCustomFieldsForProjectAndEntity(long projectId, BindableEntity entity) {
		return customFieldBindingDao.findAllForProjectAndEntity(projectId, entity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomFieldBinding> findCustomFieldsForBoundEntity(BoundEntity boundEntity) {
		return customFieldBindingDao.findAllForProjectAndEntity(boundEntity.getProject().getId(),
				boundEntity.getBoundEntityType());
	}

	@Override
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<CustomFieldBinding>> findCustomFieldsForProjectAndEntity(long projectId,
			BindableEntity entity, Paging paging) {

		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForProjectAndEntity(projectId, entity, paging);
		Long count = customFieldBindingDao.countAllForProjectAndEntity(projectId, entity);

		return new PagingBackedPagedCollectionHolder<>(paging, count, bindings);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER')" + OR_HAS_ROLE_ADMIN)
	// TODO add check for permission MANAGEMENT on the project id
	public void addNewCustomFieldBinding(long projectId, BindableEntity entity, long customFieldId,
			CustomFieldBinding newBinding) {
		createBinding(projectId, entity, customFieldId, newBinding);
		customValueService.cascadeCustomFieldValuesCreation(newBinding);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER')" + OR_HAS_ROLE_ADMIN)
	public void addRenderingLocation(long bindingId, RenderingLocation location) {
		CustomFieldBinding binding = customFieldBindingDao.findById(bindingId);
		binding.addRenderingLocation(location);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER')" + OR_HAS_ROLE_ADMIN)
	public void removeRenderingLocation(long bindingId, RenderingLocation location) {
		CustomFieldBinding binding = customFieldBindingDao.findById(bindingId);
		binding.removeRenderingLocation(location);
	}

	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER')" + OR_HAS_ROLE_ADMIN)
	public void removeCustomFieldBindings(List<Long> bindingIds) {
		customValueService.cascadeCustomFieldValuesDeletion(bindingIds);
		customFieldBindingDao.removeCustomFieldBindings(bindingIds);
		eventPublisher.publishEvent(new DeleteCustomFieldBindingEvent(bindingIds));
	}

	@SuppressWarnings("unchecked")
	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER')" + OR_HAS_ROLE_ADMIN)
	// TODO add check for permission MANAGEMENT on the project id
	public void removeCustomFieldBindings(Long projectId) {
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForGenericProject(projectId);
		List<Long> bindingIds = new LinkedList<>(CollectionUtils.collect(bindings, BINDING_ID_COLLECTOR));
		removeCustomFieldBindings(bindingIds);
	}

	/**
	 * @see CustomFieldBindingModificationService#copyCustomFieldsSettingsFromTemplate(Project, ProjectTemplate)
	 */
	@Override
	@PreAuthorize("hasRole('ROLE_TM_PROJECT_MANAGER')" + OR_HAS_ROLE_ADMIN)
	public void moveCustomFieldbindings(List<Long> bindingIds, int newIndex) {

		if (!bindingIds.isEmpty()) {

			List<CustomFieldBinding> bindingList = customFieldBindingDao.findAllAlike(bindingIds.get(0));
			PositionAwareBindingList reorderList = new PositionAwareBindingList(bindingList);
			reorderList.reorderItems(bindingIds, newIndex);

		}

	}

	private void createBinding(long projectId, BindableEntity entity, long customFieldId, CustomFieldBinding newBinding) {

		GenericProject project = genericProjectDao.findById(projectId);
		CustomField field = customFieldDao.findById(customFieldId);
		Long newIndex = customFieldBindingDao.countAllForProjectAndEntity(projectId, entity) + 1;

		newBinding.setBoundProject(project);
		newBinding.setBoundEntity(entity);
		newBinding.setCustomField(field);
		newBinding.setPosition(newIndex.intValue());

		customFieldBindingDao.save(newBinding);
		eventPublisher.publishEvent(new CreateCustomFieldBindingEvent(newBinding));
	}

	/**
	 * @see CustomFieldBindingModificationService#copyCustomFieldsSettingsFromTemplate(Project, ProjectTemplate)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void copyCustomFieldsSettingsFromTemplate(GenericProject target, GenericProject source) {
		List<CustomFieldBinding> templateCutomFieldBindings = findCustomFieldsForGenericProject(source.getId());
		for (CustomFieldBinding templateCustomFieldBinding : templateCutomFieldBindings) {
			long projectId = target.getId();
			BindableEntity entity = templateCustomFieldBinding.getBoundEntity();
			long customFieldId = templateCustomFieldBinding.getCustomField().getId();
			CustomFieldBinding newBinding = new CustomFieldBinding();
			newBinding.setRenderingLocations( templateCustomFieldBinding.copyRenderingLocations());
			addNewCustomFieldBinding(projectId, entity, customFieldId, newBinding);
		}

	}

}
