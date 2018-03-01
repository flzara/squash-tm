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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.customfield.*;
import org.squashtest.tm.domain.customfield.CustomFieldBinding.PositionAwareBindingList;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.event.CreateCustomFieldBindingEvent;
import org.squashtest.tm.event.DeleteCustomFieldBindingEvent;
import org.squashtest.tm.exception.project.LockedParameterException;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.CustomFieldDao;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;

import javax.inject.Inject;
import java.util.*;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

@Service("squashtest.tm.service.CustomFieldBindingService")
@Transactional
public class CustomFieldBindingModificationServiceImpl implements CustomFieldBindingModificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomFieldBindingModificationService.class);

	@Inject
	private CustomFieldDao customFieldDao;

	@Inject
	private CustomFieldBindingDao customFieldBindingDao;

	@Inject
	private PrivateCustomFieldValueService customValueService;

	@Inject
	private GenericProjectDao genericProjectDao;

	@Inject
	private ProjectDao projectDao;

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
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void createNewBindings(CustomFieldBindingModel[] bindingModels) {

		for (CustomFieldBindingModel model : bindingModels) {

			long projectId = model.getProjectId();
			if(genericProjectDao.isBoundToATemplate(projectId)) {
				throw new LockedParameterException();
			}
			long fieldId = model.getCustomField().getId();
			BindableEntity entity = model.getBoundEntity().toDomain();

			addNewCustomFieldBinding(projectId, entity, fieldId, null);

			/* Modifications propagation to bound Projects if the GenericProject is a Template. */
			if(genericProjectDao.isProjectTemplate(projectId)) {
				propagateCufBindingCreationToBoundProjects(projectId, entity, fieldId);
			}
		}
	}

	private void propagateCufBindingCreationToBoundProjects(long templateId, BindableEntity entity, long cufId) {
		Collection<Long> boundProjectsIds = projectDao.findAllIdsBoundToTemplate(templateId);
		for(Long boundProjectId : boundProjectsIds) {
			if(!customFieldBindingDao.cufBindingAlreadyExists(boundProjectId, entity, cufId)) {
				addNewCustomFieldBinding(boundProjectId, entity, cufId, null);
			}
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	// TODO add check for permission MANAGEMENT on the project id
	public void addNewCustomFieldBinding(long projectId, BindableEntity entity, long customFieldId,
										 Set<RenderingLocation> locations) {

		GenericProject genericProject = genericProjectDao.findOne(projectId);
		CustomFieldBinding newBinding = createBinding(genericProject, entity, customFieldId, locations);
		/* Create all the cufValues for the existing Entities. */
		if (!genericProjectDao.isProjectTemplate(projectId)) {
			customValueService.cascadeCustomFieldValuesCreation(newBinding);
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void addRenderingLocation(long bindingId, RenderingLocation location) {
		CustomFieldBinding binding = customFieldBindingDao.findById(bindingId);
		binding.addRenderingLocation(location);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void removeRenderingLocation(long bindingId, RenderingLocation location) {
		CustomFieldBinding binding = customFieldBindingDao.findById(bindingId);
		binding.removeRenderingLocation(location);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void removeCustomFieldBindings(List<Long> bindingIds) {
		if(!bindingIds.isEmpty()) {
			if(genericProjectDao.oneIsBoundToABoundProject(bindingIds)) {
				throw new LockedParameterException();
			}
			doRemoveCustomFieldBindings(bindingIds);
		}
	}

	@Override
	public void doRemoveCustomFieldBindings(List<Long> bindingIds) {
		/* If the given bindings are removed from a ProjectTemplate, we have to propagate the deletion to the
		 * equivalent bindings in the bound Projects. */
		if(!bindingIds.isEmpty()) {
			List<Long> bindingIdsToRemove = new ArrayList<>(bindingIds);
			bindingIdsToRemove.addAll(customFieldBindingDao.findEquivalentBindingsForBoundProjects(bindingIds));

			customValueService.cascadeCustomFieldValuesDeletion(bindingIdsToRemove);
			customFieldBindingDao.removeCustomFieldBindings(bindingIdsToRemove);
			eventPublisher.publishEvent(new DeleteCustomFieldBindingEvent(bindingIdsToRemove));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	// TODO add check for permission MANAGEMENT on the project id
	public void removeCustomFieldBindings(Long projectId) {
		List<CustomFieldBinding> bindings = customFieldBindingDao.findAllForGenericProject(projectId);
		List<Long> bindingIds = new LinkedList<>(CollectionUtils.collect(bindings, BINDING_ID_COLLECTOR));
		doRemoveCustomFieldBindings(bindingIds);
	}

	/**
	 * @see CustomFieldBindingModificationService#copyCustomFieldsSettingsFromTemplate(GenericProject, GenericProject)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void moveCustomFieldbindings(List<Long> bindingIds, int newIndex) {

		if (!bindingIds.isEmpty()) {

			List<CustomFieldBinding> bindingList = customFieldBindingDao.findAllAlike(bindingIds.get(0));
			PositionAwareBindingList reorderList = new PositionAwareBindingList(bindingList);
			reorderList.reorderItems(bindingIds, newIndex);

		}

	}

	private CustomFieldBinding createBinding(GenericProject genericProject, BindableEntity entity, long customFieldId,
							   Set<RenderingLocation> locations) {

			CustomFieldBinding newBinding = new CustomFieldBinding();
			CustomField field = customFieldDao.findById(customFieldId);
			Long newIndex = customFieldBindingDao.countAllForProjectAndEntity(genericProject.getId(), entity) + 1;

			newBinding.setBoundProject(genericProject);
			newBinding.setBoundEntity(entity);
			newBinding.setCustomField(field);
			newBinding.setPosition(newIndex.intValue());
			if(locations != null) { newBinding.setRenderingLocations(locations); }

			customFieldBindingDao.save(newBinding);
			eventPublisher.publishEvent(new CreateCustomFieldBindingEvent(newBinding));

			return newBinding;
	}

	/**
	 * @see CustomFieldBindingModificationService#copyCustomFieldsSettingsFromTemplate(GenericProject, GenericProject)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void copyCustomFieldsSettingsFromTemplate(GenericProject target, GenericProject source) {

		List<CustomFieldBinding> templateCustomFieldBindings = findCustomFieldsForGenericProject(source.getId());
		for (CustomFieldBinding templateCustomFieldBinding : templateCustomFieldBindings) {
			long projectId = target.getId();
			BindableEntity entity = templateCustomFieldBinding.getBoundEntity();
			long customFieldId = templateCustomFieldBinding.getCustomField().getId();

			if (!customFieldBindingDao.cufBindingAlreadyExists(projectId, entity, customFieldId)) {
				Set<RenderingLocation> locations = templateCustomFieldBinding.copyRenderingLocations();
				addNewCustomFieldBinding(projectId, entity, customFieldId, locations);
			}
		}
	}
}
