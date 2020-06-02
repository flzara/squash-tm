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
package org.squashtest.tm.service.internal.actionword;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.actionword.ActionWordLibrary;
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeDefinition;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntity;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.customreport.CustomReportTreeEntity;
import org.squashtest.tm.domain.customreport.CustomReportTreeLibraryNode;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.actionword.ActionWordLibraryNodeService;
import org.squashtest.tm.service.internal.repository.ActionWordLibraryNodeDao;
import org.squashtest.tm.service.internal.testcase.bdd.ActionWordParser;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service
@Transactional
public class ActionWordLibraryNodeServiceImpl implements ActionWordLibraryNodeService {

	@Inject
	private ActionWordLibraryNodeDao actionWordLibraryNodeDao;

	@Override
	public ActionWordLibraryNode findActionWordLibraryNodeById(Long nodeId) {
		return actionWordLibraryNodeDao.getOne(nodeId);
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#nodeId, 'org.squashtest.tm.domain.actionword.ActionWordLibraryNode', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	public ActionWordLibrary findLibraryByNodeId(Long nodeId) {
		return (ActionWordLibrary) findEntityAndCheckType(nodeId, ActionWordTreeDefinition.LIBRARY);
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#nodeId, 'org.squashtest.tm.domain.actionword.ActionWordLibraryNode', 'READ') "
		+ OR_HAS_ROLE_ADMIN)
	public ActionWord findActionWordByNodeId(Long nodeId) {
		return (ActionWord) findEntityAndCheckType(nodeId, ActionWordTreeDefinition.ACTION_WORD);
	}

	@Override
	@PreAuthorize("hasPermission(#parentId, 'org.squashtest.tm.domain.actionword.ActionWordLibraryNode', 'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public ActionWordLibraryNode createNewNode(Long parentId, ActionWordTreeEntity entity)
		throws NameAlreadyInUseException {
		ActionWordLibraryNode parentNode = actionWordLibraryNodeDao.getOne(parentId);

		ActionWordLibraryNode newNode = new ActionWordLibraryNodeBuilder(parentNode, entity).build();
		return actionWordLibraryNodeDao.save(newNode);
	}

	@Override
	public ActionWordLibraryNode findNodeFromEntity(ActionWordTreeEntity actionWordTreeEntity) {
		return actionWordLibraryNodeDao.findNodeFromEntity(actionWordTreeEntity);
	}

	private ActionWordTreeEntity findEntityAndCheckType(Long nodeId, ActionWordTreeDefinition entityType){
		ActionWordLibraryNode node = findActionWordLibraryNodeById(nodeId);

		if (node == null || node.getEntityType() != entityType) {
			String message = "The node of id %d doesn't exist or doesn't represent a %s entity.";
			throw new IllegalArgumentException(String.format(message, nodeId, entityType.getTypeName()));
		}

		ActionWordTreeEntity entity = node.getEntity();
		if (entity == null) {
			String message = "The node of id %d represents a null entity.";
			throw new IllegalArgumentException(String.format(message, nodeId));
		}
		return entity;
	}

	@Override
	public void renameNodeFromActionWord(ActionWord actionWord) {
		ActionWordLibraryNode actionWordLibraryNode = findNodeFromEntity(actionWord);
		actionWordLibraryNode.renameNode(
			new ActionWordParser().createWord(
				actionWord.getFragments()));
	}
}
