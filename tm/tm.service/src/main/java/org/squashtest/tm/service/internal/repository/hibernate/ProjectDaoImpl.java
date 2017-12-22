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

import java.util.Collections;
import java.util.List;

import org.jooq.DSLContext;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.repository.CustomProjectDao;
import org.squashtest.tm.service.internal.repository.ParameterNames;

import javax.inject.Inject;

import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.jooq.domain.Tables.ACL_OBJECT_IDENTITY;


public class ProjectDaoImpl extends HibernateEntityDao<Project> implements CustomProjectDao {

	@Inject
	private DSLContext DSL;

	@Override
	public long countNonFoldersInProject(long projectId) {
		Long req = (Long) executeEntityNamedQuery("project.countNonFolderInRequirement", idParameter(projectId));
		Long tc = (Long) executeEntityNamedQuery("project.countNonFolderInTestCase", idParameter(projectId));
		Long camp = (Long) executeEntityNamedQuery("project.countNonFolderInCampaign", idParameter(projectId));
		Long customReport = (Long) executeEntityNamedQuery("project.countNonFolderInCustomReport", idParameter(projectId));

		return req + tc + camp + customReport;
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
}
