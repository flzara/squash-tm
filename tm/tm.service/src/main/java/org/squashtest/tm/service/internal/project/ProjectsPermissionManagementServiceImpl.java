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

import org.springframework.data.domain.Sort;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.project.*;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.PartyDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("squashtest.tm.service.ProjectsPermissionManagementService")
@Transactional
public class ProjectsPermissionManagementServiceImpl implements ProjectsPermissionManagementService {

	private static final String NAMESPACE = "squashtest.acl.group.tm";
	private static final List<String> PROJECT_CLASS_NAMES = new ArrayList<>(Arrays.asList(
		"org.squashtest.tm.domain.project.Project", "org.squashtest.tm.domain.project.ProjectTemplate"));

	@Inject
	private ObjectAclService aclService;

	@Inject
	private GenericProjectDao genericProjectFinder;

	@Inject
	private UserDao userDao;

	@Inject
	private PartyDao partyDao;

	@Transactional(readOnly = true)
	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return aclService.findAllPermissionGroupsByNamespace(NAMESPACE);
	}

	@Override
	public void deleteUserProjectOldPermission(String userLogin, long projectId) {
		ObjectIdentity entityRef = createProjectIdentity(projectId);
		User user = userDao.findUserByLogin(userLogin);

		aclService.removeAllResponsibilities(user.getId(), entityRef);

		GenericProject project = genericProjectFinder.findOne(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.removeAllResponsibilities(user.getId(), rlibraryRef);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.removeAllResponsibilities(user.getId(), tclibraryRef);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.removeAllResponsibilities(user.getId(), clibraryRef);

		ObjectIdentity crlibraryRef = createCustomReportLibraryIdentity(project);
		aclService.removeAllResponsibilities(user.getId(), crlibraryRef);
	}

	private ObjectIdentity createProjectIdentity(long projectId) {
		GenericProject project = genericProjectFinder.findOne(projectId);
		final Class<?>[] projectType = {null};

		project.accept(new ProjectVisitor() {

			@Override
			public void visit(ProjectTemplate projectTemplate) {
				projectType[0] = ProjectTemplate.class;
			}

			@Override
			public void visit(Project project) {
				projectType[0] = Project.class;
			}
		});

		return new ObjectIdentityImpl(projectType[0], projectId);
	}

	private ObjectIdentity createCampaignLibraryIdentity(GenericProject project) {
		return new ObjectIdentityImpl(CampaignLibrary.class, project.getCampaignLibrary().getId());
	}

	private ObjectIdentity createTestCaseLibraryIdentity(GenericProject project) {
		return new ObjectIdentityImpl(TestCaseLibrary.class, project.getTestCaseLibrary()
			.getId());
	}

	private ObjectIdentity createRequirementLibraryIdentity(GenericProject project) {
		return new ObjectIdentityImpl(RequirementLibrary.class, project.getRequirementLibrary().getId());
	}

	private ObjectIdentity createCustomReportLibraryIdentity(GenericProject project) {
		return new ObjectIdentityImpl(CustomReportLibrary.class, project.getCustomReportLibrary().getId());
	}

	@Transactional(readOnly = true)
	@Override
	public List<ProjectPermission> findProjectPermissionByParty(long partyId) {
		List<ProjectPermission> newResult = new ArrayList<>();
		List<Object[]> result = aclService.retrieveClassAclGroupFromPartyId(partyId, PROJECT_CLASS_NAMES);
		for (Object[] objects : result) {
			GenericProject project = genericProjectFinder.findOne((Long) objects[0]);
			newResult.add(new ProjectPermission(project, (PermissionGroup) objects[1]));
		}
		return newResult;
	}

	@Transactional(readOnly = true)
	@Override
	public List<GenericProject> findProjectWithPermissionByParty(long partyId) {
		List<GenericProject> newResult = new ArrayList<>();
		List<Object[]> result = aclService.retrieveClassAclGroupFromPartyId(partyId, PROJECT_CLASS_NAMES);
		for (Object[] objects : result) {
			GenericProject project = genericProjectFinder.findOne((Long) objects[0]);
			newResult.add(project);
		}
		return newResult;
	}

	@Transactional(readOnly = true)
	@Override
	public List<ProjectPermission> findProjectPermissionByUserLogin(String userLogin) {
		List<ProjectPermission> newResult = new ArrayList<>();
		List<Object[]> result = aclService.retrieveClassAclGroupFromUserLogin(userLogin, PROJECT_CLASS_NAMES);
		for (Object[] objects : result) {
			GenericProject project = genericProjectFinder.findOne((Long) objects[0]);
			newResult.add(new ProjectPermission(project, (PermissionGroup) objects[1]));
		}
		return newResult;
	}

	@Transactional(readOnly = true)
	@Override
	public PagedCollectionHolder<List<ProjectPermission>> findProjectPermissionByParty(long partyId,
		PagingAndSorting sorting, Filtering filtering) {

		List<ProjectPermission> newResult = new ArrayList<>();

		List<Object[]> result = aclService.retrieveClassAclGroupFromPartyId(partyId, PROJECT_CLASS_NAMES, sorting,
			filtering);
		int total = result.size();

		for (Object[] objects : result) {
			GenericProject project = genericProjectFinder.findOne((Long) objects[0]);
			newResult.add(new ProjectPermission(project, (PermissionGroup) objects[1]));
		}

		return new PagingBackedPagedCollectionHolder<>(sorting, total, newResult);
	}

	@Transactional(readOnly = true)
	@Override
	public List<GenericProject> findProjectWithoutPermissionByParty(long partyId) {
		List<Long> idList = aclService.findObjectWithoutPermissionByPartyId(partyId, PROJECT_CLASS_NAMES);
		if (idList == null || idList.isEmpty()) {
			return null;
		}
		return genericProjectFinder.findAll(idList);

	}

	@Transactional(readOnly = true)
	@Override
	public List<GenericProject> findProjectWithoutPermissionByParty(long partyId, Sort sorting) {
		List<Long> idList = aclService.findObjectWithoutPermissionByPartyId(partyId, PROJECT_CLASS_NAMES);
		if (idList == null || idList.isEmpty()) {
			return null;
		}
		return genericProjectFinder.findAllByIdIn(idList, sorting);
	}

	@Override
	public void addNewPermissionToProject(long partyId, long projectId, String permissionName) {
		ObjectIdentity projectRef = createProjectIdentity(projectId);
		Party party = partyDao.findOne(partyId);
		aclService.addNewResponsibility(party.getId(), projectRef, permissionName);

		GenericProject project = genericProjectFinder.findOne(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.addNewResponsibility(party.getId(), rlibraryRef, permissionName);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.addNewResponsibility(party.getId(), tclibraryRef, permissionName);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.addNewResponsibility(party.getId(), clibraryRef, permissionName);

		ObjectIdentity crlibraryRef = createCustomReportLibraryIdentity(project);
		aclService.addNewResponsibility(party.getId(), crlibraryRef, permissionName);

	}

	@Override
	public void removeProjectPermission(long partyId, long projectId) {
		ObjectIdentity projectRef = createProjectIdentity(projectId);

		aclService.removeAllResponsibilities(partyId, projectRef);

		GenericProject project = genericProjectFinder.findOne(projectId);

		ObjectIdentity rlibraryRef = createRequirementLibraryIdentity(project);
		aclService.removeAllResponsibilities(partyId, rlibraryRef);

		ObjectIdentity tclibraryRef = createTestCaseLibraryIdentity(project);
		aclService.removeAllResponsibilities(partyId, tclibraryRef);

		ObjectIdentity clibraryRef = createCampaignLibraryIdentity(project);
		aclService.removeAllResponsibilities(partyId, clibraryRef);

		ObjectIdentity crlibraryRef = createCustomReportLibraryIdentity(project);
		aclService.removeAllResponsibilities(partyId, crlibraryRef);
	}

	@Override
	public void removeProjectPermissionForAllProjects(long partyId) {
		aclService.removeAllResponsibilities(partyId);
	}

	@Transactional(readOnly = true)
	@Override
	public List<PartyProjectPermissionsBean> findPartyPermissionsBeanByProject(long projectId) {

		Class<?> projectClass = genericProjectFinder.isProjectTemplate(projectId) ? ProjectTemplate.class
			: Project.class;

		return findPartyPermissionBeanByProjectOfGivenType(projectId, projectClass);

	}

	@Transactional(readOnly = true)
	@Override
	public PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionsBeanByProject(
		PagingAndSorting sorting, Filtering filtering, long projectId) {

		Class<?> projectClass = genericProjectFinder.isProjectTemplate(projectId) ? ProjectTemplate.class
			: Project.class;

		return findPartyPermissionBeanByProjectOfGivenType(projectId, projectClass, sorting, filtering);

	}

	@Transactional(readOnly = true)
	@Override
	public List<Party> findPartyWithoutPermissionByProject(long projectId) {
		List<Long> idList = aclService.findPartiesWithoutPermissionByObject(projectId, PROJECT_CLASS_NAMES);
		return partyDao.findAll(idList);
	}

	/**
	 * @see ProjectsPermissionManagementService#copyAssignedUsersFromTemplate(Project, ProjectTemplate)
	 */
	@Override
	public void copyAssignedUsersFromTemplate(Project newProject, ProjectTemplate projectTemplate) {
		long templateId = projectTemplate.getId();
		copyAssignedUsersFromTemplate(newProject, templateId);
	}

	private List<PartyProjectPermissionsBean> findPartyPermissionsBeanByProjectTemplate(long projectId, Class<?> genericClass) {
		return findPartyPermissionBeanByProjectOfGivenType(projectId, genericClass);
	}

	// clearly suboptimal, on the other hand this method is seldomely invoked
	private List<PartyProjectPermissionsBean> findPartyPermissionBeanByProjectOfGivenType(long projectId,
		Class<?> projectType) {
		List<PartyProjectPermissionsBean> newResult = new ArrayList<>();

		List<Object[]> result = aclService.retrievePartyAndAclGroupNameFromIdentityAndClass(projectId, projectType);
		for (Object[] objects : result) {
			Party party = partyDao.findOne((Long) objects[0]);
			newResult.add(new PartyProjectPermissionsBean(party, (PermissionGroup) objects[1]));
		}
		return newResult;

	}


	// clearly suboptimal, on the other hand this method is seldomely invoked
	private PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionBeanByProjectOfGivenType(
		long projectId, Class<?> projectType, PagingAndSorting sorting, Filtering filtering) {

		List<Object[]> result = aclService.retrievePartyAndAclGroupNameFromIdentityAndClass(projectId, projectType,
			sorting, filtering);

		int total = result.size();

		int startIndex = sorting.getFirstItemIndex();
		int lastIndex = Math.min(startIndex + sorting.getPageSize(), total);

		result = result.subList(startIndex, lastIndex);

		List<PartyProjectPermissionsBean> newResult = new ArrayList<>(result.size());

		for (Object[] objects : result) {
			Party party = partyDao.findOne((Long) objects[0]);
			newResult.add(new PartyProjectPermissionsBean(party, (PermissionGroup) objects[1]));
		}

		return new PagingBackedPagedCollectionHolder<>(sorting, total, newResult);

	}

	/**
	 * @see org.squashtest.tm.service.project.ProjectsPermissionManagementService#copyAssignedUsersFromTemplate(org.squashtest.tm.domain.project.Project,
	 *      long)
	 */
	@Override
	public void copyAssignedUsersFromTemplate(Project project, long templateId) {
		List<PartyProjectPermissionsBean> templatePartyPermissions = findPartyPermissionsBeanByProjectTemplate(templateId, ProjectTemplate.class);

		addPermissionsToProject(templatePartyPermissions, project);
	}

	@Override
	public void copyAssignedUsers(GenericProject target, GenericProject source) {
		long sourceId = source.getId();
		copyAssignedUsers(target, sourceId);
	}

	/**
	 * @see org.squashtest.tm.service.project.ProjectsPermissionManagementService#removeAllPermissionsFromProjectTemplate(long)
	 */
	@Override
	public void removeAllPermissionsFromProjectTemplate(long templateId) {
		ObjectIdentity projectRef = new ObjectIdentityImpl(ProjectTemplate.class, templateId);
		aclService.removeAllResponsibilities(projectRef);
	}

	/**
	 * @see org.squashtest.tm.service.project.ProjectsPermissionManagementService#removeAllPermissionsFromObject(Class,
	 *      long)
	 */
	@Override
	public void removeAllPermissionsFromObject(Class<?> clazz, long id) {
		ObjectIdentity ref = new ObjectIdentityImpl(clazz, id);
		aclService.removeAllResponsibilities(ref);
	}

	@Override
	public boolean isInPermissionGroup(String userLogin, Long projectId, String permissionGroup) {
		User user = userDao.findUserByLogin(userLogin);
		return isInPermissionGroup(user.getId(), projectId, permissionGroup);
	}

	@Transactional(readOnly = true)
	@Override
	public boolean isInPermissionGroup(long partyId, Long projectId, String permissionGroup) {
		boolean isInGroup = false;
		List<PartyProjectPermissionsBean> permissions = findPartyPermissionsBeanByProject(projectId);
		for (PartyProjectPermissionsBean permission : permissions) {
			if (permission.getParty().getId() == partyId) {
				if (permission.getPermissionGroup().getQualifiedName().equals(permissionGroup)) {
					isInGroup = true;
				}
			}
		}

		return isInGroup;
	}

	private void copyAssignedUsers(GenericProject targetProject, long sourceId) {
		List<PartyProjectPermissionsBean> templatePartyPermissions = findPartyPermissionsBeanByProject(sourceId);

		addPermissionsToProject(templatePartyPermissions, targetProject);
	}

	private void addPermissionsToProject(List<PartyProjectPermissionsBean> partyPermissions, GenericProject project) {
		for (PartyProjectPermissionsBean partyPermission : partyPermissions) {
			long userId = partyPermission.getParty().getId();
			long projectId = project.getId();
			String permissionName = partyPermission.getPermissionGroup().getQualifiedName();
			addNewPermissionToProject(userId, projectId, permissionName);
		}
	}
}
