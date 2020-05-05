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
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntity;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.service.actionword.ActionWordLibraryNodeService;
import org.squashtest.tm.service.internal.repository.ActionWordLibraryNodeDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service
@Transactional
public class ActionWordLibraryNodeServiceImpl implements ActionWordLibraryNodeService {

	@PersistenceContext
	private EntityManager em;

	@Inject
	private ActionWordLibraryNodeDao actionWordLibraryNodeDao;

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
}
