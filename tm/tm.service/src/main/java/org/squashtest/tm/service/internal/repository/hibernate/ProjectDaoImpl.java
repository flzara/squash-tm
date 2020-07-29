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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.jooq.DSLContext;
import org.squashtest.tm.api.plugin.PluginType;
import org.squashtest.tm.domain.project.LibraryPluginBinding;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.repository.CustomProjectDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.squashtest.tm.jooq.domain.Tables.ACL_CLASS;
import static org.squashtest.tm.jooq.domain.Tables.ACL_OBJECT_IDENTITY;
import static org.squashtest.tm.jooq.domain.Tables.ACL_RESPONSIBILITY_SCOPE_ENTRY;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;


public class ProjectDaoImpl extends HibernateEntityDao<Project> implements CustomProjectDao {

	private static final String PROJECT_ID = "projectId";

	@Inject
	private DSLContext DSL;

	@PersistenceContext
	private EntityManager em;

	@Override
	public long countNonFoldersInProject(long projectId) {
		Long req = (Long) executeEntityNamedQuery("project.countNonFolderInRequirement", idParameter(projectId));
		Long tc = (Long) executeEntityNamedQuery("project.countNonFolderInTestCase", idParameter(projectId));
		Long camp = (Long) executeEntityNamedQuery("project.countNonFolderInCampaign", idParameter(projectId));
		Long customReport = (Long) executeEntityNamedQuery("project.countNonFolderInCustomReport", idParameter(projectId));
		Long aw = (Long) executeEntityNamedQuery("project.countNonFolderInActionWord", idParameter(projectId));

		return req + tc + camp + customReport + aw;
	}

	private SetQueryParametersCallback idParameter(final long id) {
		return new SetIdParameter(ParameterNames.PROJECT_ID, id);
	}

	private SetQueryParametersCallback idParameters(final List<Long> ids) {
		return new SetProjectIdsParameterCallback(ids);
	}

	@Override
	public List<String> findUsersWhoCreatedTestCases(List<Long> projectIds){
		if(projectIds.isEmpty()){
			return Collections.emptyList();
		}
		return executeListNamedQuery("Project.findAllUsersWhoCreatedTestCases", idParameters(projectIds));
	}

	@Override
	public List<String> findUsersWhoModifiedTestCases(List<Long> projectIds){
		if(projectIds.isEmpty()){
			return Collections.emptyList();
		}
		return executeListNamedQuery("Project.findAllUsersWhoModifiedTestCases", idParameters(projectIds));
	}

	@Override
	public List<String> findUsersWhoCreatedRequirementVersions(List<Long> projectIds){
		if(projectIds.isEmpty()){
			return Collections.emptyList();
		}
		return executeListNamedQuery("Project.findAllUsersWhoCreatedRequirementVersions", idParameters(projectIds));
	}

	@Override
	public List<String> findUsersWhoModifiedRequirementVersions(List<Long> projectIds){
		if(projectIds.isEmpty()){
			return Collections.emptyList();
		}
		return executeListNamedQuery("Project.findAllUsersWhoModifiedRequirementVersions", idParameters(projectIds));
	}

	@Override
	public List<Long> findAllProjectIds() {
		return DSL.select(PROJECT.PROJECT_ID)
			.from(PROJECT)
			.where(PROJECT.PROJECT_TYPE.eq("P"))
			.fetch(PROJECT.PROJECT_ID, Long.class);
	}

	@Override
	public List<Long> findAllProjectIds(List<Long> partyIds) {
		return DSL
			.selectDistinct(ACL_OBJECT_IDENTITY.IDENTITY)
			.from(ACL_RESPONSIBILITY_SCOPE_ENTRY)
				.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
				.join(ACL_CLASS).on(ACL_CLASS.ID.eq(ACL_OBJECT_IDENTITY.CLASS_ID))
			.where(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.in(partyIds)
				.and(ACL_CLASS.CLASSNAME.eq("org.squashtest.tm.domain.project.Project")))
			.fetch(ACL_OBJECT_IDENTITY.IDENTITY, Long.class);
	}

	@Override
	public List<Long> findAllProjectIdsForAutomationWriter(List<Long> partyIds) {
		return DSL
			.selectDistinct(ACL_OBJECT_IDENTITY.IDENTITY)
			.from(ACL_RESPONSIBILITY_SCOPE_ENTRY)
			.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
			.join(ACL_CLASS).on(ACL_CLASS.ID.eq(ACL_OBJECT_IDENTITY.CLASS_ID))
			.where(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.in(partyIds)
				.and(ACL_CLASS.CLASSNAME.eq("org.squashtest.tm.domain.project.Project"))
				.and(ACL_RESPONSIBILITY_SCOPE_ENTRY.ACL_GROUP_ID.in(Arrays.asList(5L, 10L))))
			.fetch(ACL_OBJECT_IDENTITY.IDENTITY, Long.class);
	}

	@Override
	public Integer countProjectsAllowAutomationWorkflow() {
		return DSL
			.selectCount()
			.from(PROJECT)
			.where(PROJECT.ALLOW_AUTOMATION_WORKFLOW.eq(true))
			.fetchOne().value1();
	}

	@Override
	public LibraryPluginBinding findPluginForProject(Long projectId, PluginType pluginType) {
		LibraryPluginBinding lpb;
		javax.persistence.Query query = entityManager.createNamedQuery("Project.findPluginForProject");
		query.setParameter(PROJECT_ID, projectId);
		query.setParameter("pluginType",pluginType);
		try{
			lpb = (LibraryPluginBinding) query.getSingleResult();
		}catch(NoResultException nre){
			return null;
		}
		return lpb;
	}

	@Override
	public void removeLibraryPluginBindingProperty(Long libraryPluginBindingId) {
		javax.persistence.Query query = entityManager.createNativeQuery(NativeQueries.DELETE_LIBRARY_PLUGING_PINDING_PROPERTY);
		query.setParameter("libraryPluginBindingId", libraryPluginBindingId);
		query.executeUpdate();
	}

	@Override
	public BigInteger countActivePluginInProject(long projectId) {
		Query query = em.createNativeQuery(NativeQueries.COUNT_ACTIVE_PLUGIN_IN_PROJECT);
		query.setParameter(PROJECT_ID, projectId);
		return (BigInteger) query.getSingleResult();
	}

	@Override
	public Project fetchForAutomatedExecutionCreation(long projectId) {
		Query query = em.createNamedQuery("Project.fetchForAutomatedExecutionCreation");
		query.setParameter(PROJECT_ID, projectId);
		return (Project) query.getSingleResult();
	}

}
