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
package org.squashtest.tm.service.internal.project;

import org.jooq.DSLContext;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.customfield.CustomFieldModelService;
import org.squashtest.tm.service.infolist.InfoListModelService;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsonInfoList;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.internal.dto.json.JsonProject;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.project.CustomProjectModificationService;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

/**
 * @author mpagnon
 */
@Service("CustomProjectModificationService")
@Transactional
public class CustomProjectModificationServiceImpl implements CustomProjectModificationService {

	@Inject
	private ProjectDeletionHandler projectDeletionHandler;

	@Inject
	private ProjectTemplateDao projectTemplateDao;

	@Inject
	private GenericProjectManagerService genericProjectManager;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private GenericProjectDao genericProjectDao;

	@Inject
	protected UserAccountService userAccountService;

	@Inject
	private DSLContext DSL;

	@Inject
	private MilestoneModelService milestoneModelService;

	@Inject
	private CustomFieldModelService customFieldModelService;

	@Inject
	private InfoListModelService infoListModelService;


	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public List<Project> findAllReadable() {
		return projectDao.findAll();
	}

	@Override
	public Project addProjectFromTemplate(Project newProject, long templateId, GenericProjectCopyParameter params)
		throws NameAlreadyInUseException {

		genericProjectManager.persist(newProject);

		ProjectTemplate projectTemplate = projectTemplateDao.getOne(templateId);
		if(params.isKeepTemplateBinding()) {
			newProject.setTemplate(projectTemplate);
			makeParamsConsistent(params);
		}

		genericProjectManager.synchronizeGenericProject(newProject, projectTemplate, params);

		return newProject;
	}

	/* If binding with Template is kept, some parameters must be copied. */
	private void makeParamsConsistent(GenericProjectCopyParameter params) {
		params.setCopyCUF(true);
		params.setCopyInfolists(true);
		params.setCopyAllowTcModifFromExec(true);
		params.setCopyOptionalExecStatuses(true);
	}

	@Override
	public List<GenericProject> findAllICanManage() {
		List<GenericProject> projects = genericProjectDao.findAll();
		List<GenericProject> manageableProjects = new ArrayList<>();

		for (GenericProject project : projects) {
			if (permissionEvaluationService.hasRoleOrPermissionOnObject("ADMIN", "MANAGEMENT", project)) {
				manageableProjects.add(project);
			}
		}
		return manageableProjects;
	}

	/**
	 * Optimized implementation with SQL and no hibernate entities.
	 * @param userDto
	 */
	@Override
	public List<Long> findAllReadableIds(UserDto userDto) {
		if (userDto.isAdmin()) {
			return projectDao.findAllProjectIds();
		} else {
			return projectDao.findAllProjectIds(userDto.getPartyIds());
		}
	}

	/**
	 * Optimized implementation with SQL and no hibernate entities.
	 */
	@Override
	public List<Long> findAllReadableIdsForAutomationWriter() {
		UserDto currentUser = userAccountService.findCurrentUserDto();
		if (currentUser.isAdmin()) {
			return projectDao.findAllProjectIds();
		} else {
			return projectDao.findAllProjectIdsForAutomationWriter(currentUser.getPartyIds());
		}
	}

	@Override
	public List<Long> findAllReadableIds() {
		UserDto currentUser = userAccountService.findCurrentUserDto();
		return findAllReadableIds(currentUser);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<Project> findAllOrderedByName() {
		return projectDao.findAllByOrderByName();
	}

	@Override
	public Collection<JsonProject> findAllProjects(List<Long> readableProjectIds, UserDto currentUser) {
		Map<Long, JsonProject> jsonProjects = doFindAllProjects(readableProjectIds);
		return jsonProjects.values();
	}

	@Override
	public Integer countProjectsAllowAutomationWorkflow() {
		return projectDao.countProjectsAllowAutomationWorkflow();
	}

	protected Map<Long, JsonProject> doFindAllProjects(List<Long> readableProjectIds) {
		// As projects are objects with complex relationship we pre fetch some of the relation to avoid unnecessary joins or requests, and unnecessary conversion in DTO after fetch
		// We do that only on collaborators witch should not be too numerous versus the number of projects
		// good candidate for this pre fetch are infolists, custom fields (not bindings), milestones...
 		Map<Long, JsonInfoList> infoListMap = infoListModelService.findUsedInfoList(readableProjectIds);

		Map<Long, JsonProject> jsonProjectMap = findJsonProjects(readableProjectIds, infoListMap);

		// Now we retrieve the bindings for projects, injecting cuf inside
		Map<Long, Map<String, List<CustomFieldBindingModel>>> customFieldsBindingsByProject = customFieldModelService.findCustomFieldsBindingsByProject(readableProjectIds);

		// We find the milestone bindings and provide projects with them
		Map<Long, List<JsonMilestone>> milestoneByProjectId = milestoneModelService.findMilestoneByProject(readableProjectIds);

		// We provide the projects with their bindings and milestones
		jsonProjectMap.forEach((projectId, jsonProject) -> {
			if (customFieldsBindingsByProject.containsKey(projectId)) {
				Map<String, List<CustomFieldBindingModel>> bindingsByEntityType = customFieldsBindingsByProject.get(projectId);
				jsonProject.setCustomFieldBindings(bindingsByEntityType);
			}

			if (milestoneByProjectId.containsKey(projectId)) {
				List<JsonMilestone> jsonMilestone = milestoneByProjectId.get(projectId);
				jsonProject.setMilestones(new HashSet<>(jsonMilestone));
			}
		});

		return jsonProjectMap;
	}

	private Map<Long, JsonProject> findJsonProjects(List<Long> readableProjectIds, Map<Long, JsonInfoList> infoListMap) {
		return DSL.select(PROJECT.PROJECT_ID, PROJECT.NAME, PROJECT.REQ_CATEGORIES_LIST, PROJECT.TC_NATURES_LIST, PROJECT.TC_TYPES_LIST)
			.from(PROJECT)
			.where(PROJECT.PROJECT_ID.in(readableProjectIds)).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
			.orderBy(PROJECT.PROJECT_ID)
			.stream()
			.map(r -> {
				Long projectId = r.get(PROJECT.PROJECT_ID);
				JsonProject jsonProject = new JsonProject(projectId, r.get(PROJECT.NAME));
				jsonProject.setRequirementCategories(infoListMap.get(r.get(PROJECT.REQ_CATEGORIES_LIST)));
				jsonProject.setTestCaseNatures(infoListMap.get(r.get(PROJECT.TC_NATURES_LIST)));
				jsonProject.setTestCaseTypes(infoListMap.get(r.get(PROJECT.TC_TYPES_LIST)));
				return jsonProject;

			}).collect(Collectors.toMap(JsonProject::getId, Function.identity(),
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				},
				LinkedHashMap::new));
	}
}
