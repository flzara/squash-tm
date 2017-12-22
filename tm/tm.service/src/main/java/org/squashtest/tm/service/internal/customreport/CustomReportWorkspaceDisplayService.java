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
package org.squashtest.tm.service.internal.customreport;

import org.apache.commons.collections.MultiMap;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateEntityDao;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.squashtest.tm.jooq.domain.Tables.CUSTOM_REPORT_LIBRARY;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;

@Service("customReportWorkspaceDisplayService")
@Transactional(readOnly = true)
public class CustomReportWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Inject
	DSLContext DSL;

	@Override
	protected Map<Long, JsTreeNode> getLibraryChildrenMap(Set<Long> childrenIds, MultiMap expansionCandidates, UserDto currentUser, Map<Long, List<Long>> allMilestonesForLN, List<Long> milestonesModifiable, Long activeMilestoneId) {
		return null;
	}

	// *************************************** send stuff to abstract workspace ***************************************

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.CRL_ID;
	}

	@Override
	protected String getFolderName() {
		return null;
	}

	@Override
	protected Object getNodeName() {
		return null;
	}

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return CUSTOM_REPORT_LIBRARY.CRL_ID;
	}

	@Override
	protected Map<Long, List<Long>> findAllMilestonesForLN() {
		return null;
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return CUSTOM_REPORT_LIBRARY;
	}

	@Override
	protected TableLike<?> getLibraryTableContent() {
		return null;
	}

	@Override
	protected Field<Long> selectLNRelationshipAncestorId() {
		return null;
	}

	@Override
	protected Field<Long> selectLNRelationshipDescendantId() {
		return null;
	}

	@Override
	protected Field<Integer> selectLNRelationshipContentOrder() {
		return null;
	}

	@Override
	protected TableLike<?> getLNRelationshipTable() {
		return null;
	}

	@Override
	protected Field<Long> getMilestoneLibraryNodeId() {
		return null;
	}

	@Override
	protected TableLike<?> getMilestoneLibraryNodeTable() {
		return null;
	}

	@Override
	protected Field<Long> getMilestoneId() {
		return null;
	}

	@Override
	protected HibernateEntityDao hibernateFolderDao() {
		return null;
	}

	@Override
	protected Set<Long> findLNByMilestoneId(Long activeMilestoneId) {
		return null;
	}

	@Override
	protected boolean passesMilestoneFilter(JsTreeNode node, Long activeMilestoneId) {
		return false;
	}

	@Override
	protected Field<Long> selectLibraryContentContentId() {
		return null;
	}

	@Override
	protected Field<Integer> selectLibraryContentOrder() {
		return null;
	}

	@Override
	protected Field<Long> selectLibraryContentLibraryId() {
		return null;
	}

	@Override
	protected String getClassName() {
		return CustomReportLibrary.class.getSimpleName();
	}

	@Override
	protected String getLibraryClassName() {
		return CustomReportLibrary.class.getName();
	}

	@Override
	protected String getLibraryPluginType() {
		throw new RuntimeException("No plugin library of type Custom Report exists in squash tm");
	}

	@Override
	public Collection<JsTreeNode> getCampaignNodeContent(Long folderId, UserDto currentUser, String libraryNode) {
		return null;
	}
}
