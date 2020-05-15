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

import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntity;
import org.squashtest.tm.domain.actionword.GetActionWordTreeDefinitionVisitor;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.GetCustomReportTreeDefinitionVisitor;
import org.squashtest.tm.service.internal.repository.CustomActionWordLibraryNodeDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;


public class ActionWordLibraryNodeDaoImpl implements CustomActionWordLibraryNodeDao {

	@PersistenceContext
	EntityManager em;

	@Override
	public ActionWordLibraryNode findNodeFromEntity(ActionWordTreeEntity actionWordTreeEntity) {
		GetActionWordTreeDefinitionVisitor visitor = new GetActionWordTreeDefinitionVisitor();
		actionWordTreeEntity.accept(visitor);
		Query query = em.createNamedQuery("ActionWordLibraryNode.findNodeFromEntity");
		query.setParameter("entityType", visitor.getActionWordTreeDefinition());
		query.setParameter("entityId", actionWordTreeEntity.getId());
		return (ActionWordLibraryNode) query.getSingleResult();
	}
}
