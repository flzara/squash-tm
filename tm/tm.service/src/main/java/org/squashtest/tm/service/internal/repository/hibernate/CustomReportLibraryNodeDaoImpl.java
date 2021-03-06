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

import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeEntity;
import org.squashtest.tm.domain.customreport.CustomReportTreeLibraryNode;
import org.squashtest.tm.domain.customreport.GetCustomReportTreeDefinitionVisitor;
import org.squashtest.tm.service.internal.repository.CustomCustomReportLibraryNodeDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class CustomReportLibraryNodeDaoImpl implements CustomCustomReportLibraryNodeDao {

	private static final String UNCHECKED = "unchecked";

	@PersistenceContext
	EntityManager em;

	@Override
	public List<CustomReportTreeLibraryNode> findChildren(Long parentId) {
		CustomReportLibraryNode node = em.find(CustomReportLibraryNode.class,parentId);
		return node.getChildren();
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<Long> findAllDescendantIds(List<Long> nodesIds) {
		Query query = em.createNamedQuery("CustomReportLibraryNodePathEdge.findAllDescendantIds");
		query.setParameter("ids", nodesIds);
		return query.getResultList();
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<CustomReportLibraryNode> findAllDescendants(List<Long> nodesIds) {
		Query query = em.createNamedQuery("CustomReportLibraryNodePathEdge.findAllDescendant");
		query.setParameter("ids", nodesIds);
		return query.getResultList();
	}


	@SuppressWarnings(UNCHECKED)
	@Override
	public List<Long> findAllFirstLevelDescendantIds(List<Long> nodesIds) {
		Query query = em.createNamedQuery("CustomReportLibraryNodePathEdge.findAllFirstLevelDescendantIds");
		query.setParameter("ids", nodesIds);
		return query.getResultList();
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<Long> findAncestorIds(Long nodeId) {
		Query query = em.createNamedQuery("CustomReportLibraryNodePathEdge.findAllAncestorIds");
		query.setParameter("id", nodeId);
		return query.getResultList();
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public List<Object[]> findAncestor(Long nodeId) {
		Query query = em.createNativeQuery("SELECT " +
			"CRLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID, " +
			"CUSTOM_REPORT_LIBRARY_NODE.ENTITY_TYPE " +
			"FROM " +
			"CRLN_RELATIONSHIP_CLOSURE " +
			"LEFT JOIN " +
			"CUSTOM_REPORT_LIBRARY_NODE ON CRLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID = CUSTOM_REPORT_LIBRARY_NODE.CRLN_ID "  +
			"WHERE " +
			"CRLN_RELATIONSHIP_CLOSURE.DESCENDANT_ID = :id");
		query.setParameter("id", nodeId);
		return query.getResultList();
	}

	@Override
	public List<Long> findAllFirstLevelDescendantIds(Long nodeId) {
		List<Long> ids = new ArrayList<>();
		ids.add(nodeId);
		return findAllFirstLevelDescendantIds(ids);
	}

	@Override
	public List<CustomReportLibraryNode> findAllConcreteLibraries(List<Long> projectIds) {
		Query query = em.createNamedQuery("CustomReportLibraryNode.findConcreteLibraryFiltered");
		query.setParameter("filteredProjectsIds", projectIds);
		return query.getResultList();
	}

	@Override
	public List<CustomReportLibraryNode> findAllConcreteLibraries() {
		Query query = em.createNamedQuery("CustomReportLibraryNode.findConcreteLibrary");
		return query.getResultList();
	}

	@Override
	public CustomReportLibraryNode findNodeFromEntity(CustomReportTreeEntity treeEntity) {
		GetCustomReportTreeDefinitionVisitor visitor = new GetCustomReportTreeDefinitionVisitor();
		treeEntity.accept(visitor);
		Query query = em.createNamedQuery("CustomReportLibraryNode.findNodeFromEntity");
		query.setParameter("entityType", visitor.getCustomReportTreeDefinition());
		query.setParameter("entityId", treeEntity.getId());
		return (CustomReportLibraryNode) query.getSingleResult();
	}

	@Override
	public Long countNodeFromEntity(CustomReportTreeEntity treeEntity) {
		GetCustomReportTreeDefinitionVisitor visitor = new GetCustomReportTreeDefinitionVisitor();
		treeEntity.accept(visitor);
		Query query = em.createNamedQuery("CustomReportLibraryNode.countNodeFromEntity");
		query.setParameter("entityType", visitor.getCustomReportTreeDefinition());
		query.setParameter("entityId", treeEntity.getId());
		return (Long) query.getSingleResult();
	}

	@Override
	public List<Long> findAllNodeIdsForLibraryEntity(Long libraryId) {
		Query query = em.createNamedQuery("CustomReportLibraryNode.findAllNodeForCustomReportLibrary");
		query.setParameter("libraryId", libraryId);
		return query.getResultList();
	}

	public Long findCurrentProjectFromCustomReportFoldersId(Long id){
		Query query = em.createNamedQuery("BoundEntityDao.findCurrentProjectFromCustomReportFoldersId");
		query.setParameter("clnId", id);
		return (Long) query.getSingleResult();
	}
}
