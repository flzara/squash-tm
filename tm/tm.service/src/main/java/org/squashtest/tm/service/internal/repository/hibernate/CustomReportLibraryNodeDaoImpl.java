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


import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.*;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.internal.repository.CustomCustomReportLibraryNodeDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;


public class CustomReportLibraryNodeDaoImpl implements CustomCustomReportLibraryNodeDao {

	@PersistenceContext
	EntityManager em;

	@Override
	public List<TreeLibraryNode> findChildren(Long parentId) {
		CustomReportLibraryNode node = em.find(CustomReportLibraryNode.class,parentId);
		return node.getChildren();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllDescendantIds(List<Long> nodesIds) {
		Query query = em.createNamedQuery("CustomReportLibraryNodePathEdge.findAllDescendantIds");
		query.setParameter("ids", nodesIds);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomReportLibraryNode> findAllDescendants(List<Long> nodesIds) {
		Query query = em.createNamedQuery("CustomReportLibraryNodePathEdge.findAllDescendant");
		query.setParameter("ids", nodesIds);
		return query.getResultList();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAllFirstLevelDescendantIds(List<Long> nodesIds) {
		Query query = em.createNamedQuery("CustomReportLibraryNodePathEdge.findAllFirstLevelDescendantIds");
		query.setParameter("ids", nodesIds);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findAncestorIds(Long nodeId) {
		Query query = em.createNamedQuery("CustomReportLibraryNodePathEdge.findAllAncestorIds");
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
	public CustomReportLibraryNode findNodeFromEntity(TreeEntity treeEntity) {
		final CustomReportTreeDefinition[] type = new CustomReportTreeDefinition[1];
		TreeEntityVisitor visitor = new TreeEntityVisitor() {

			@Override
			public void visit(ReportDefinition reportDefinition) {
				type[0] = CustomReportTreeDefinition.REPORT;
			}

			@Override
			public void visit(ChartDefinition chartDefinition) {
				type[0] = CustomReportTreeDefinition.CHART;
			}

			@Override
			public void visit(CustomReportDashboard crf) {
				type[0] = CustomReportTreeDefinition.DASHBOARD;
			}

			@Override
			public void visit(CustomReportLibrary crl) {
				type[0] = CustomReportTreeDefinition.LIBRARY;
			}

			@Override
			public void visit(CustomReportFolder crf) {
				type[0] = CustomReportTreeDefinition.FOLDER;
			}
		};
		treeEntity.accept(visitor);
		Query query = em.createNamedQuery("CustomReportLibraryNode.findNodeFromEntity");
		query.setParameter("entityType", type[0]);
		query.setParameter("entityId", treeEntity.getId());
		return (CustomReportLibraryNode) query.getSingleResult();
	}

	@Override
	public Long countNodeFromEntity(TreeEntity treeEntity) {
		final CustomReportTreeDefinition[] type = new CustomReportTreeDefinition[1];
		TreeEntityVisitor visitor = new TreeEntityVisitor() {

			@Override
			public void visit(ReportDefinition reportDefinition) {
				type[0] = CustomReportTreeDefinition.REPORT;
			}

			@Override
			public void visit(ChartDefinition chartDefinition) {
				type[0] = CustomReportTreeDefinition.CHART;
			}

			@Override
			public void visit(CustomReportDashboard crf) {
				type[0] = CustomReportTreeDefinition.DASHBOARD;
			}

			@Override
			public void visit(CustomReportLibrary crl) {
				type[0] = CustomReportTreeDefinition.LIBRARY;
			}

			@Override
			public void visit(CustomReportFolder crf) {
				type[0] = CustomReportTreeDefinition.FOLDER;
			}
		};
		treeEntity.accept(visitor);
		Query query = em.createNamedQuery("CustomReportLibraryNode.countNodeFromEntity");
		query.setParameter("entityType", type[0]);
		query.setParameter("entityId", treeEntity.getId());
		return (Long) query.getSingleResult();
	}

	@Override
	public List<Long> findAllNodeIdsForLibraryEntity(Long libraryId) {
		Query query = em.createNamedQuery("CustomReportLibraryNode.findAllNodeForCustomReportLibrary");
		query.setParameter("libraryId", libraryId);
		return query.getResultList();
	}

}
