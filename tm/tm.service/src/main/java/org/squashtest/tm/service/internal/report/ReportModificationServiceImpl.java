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
package org.squashtest.tm.service.internal.report;

import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.report.ReportModificationService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

@Service("squashtest.tm.service.ReportModificationService")
public class ReportModificationServiceImpl implements ReportModificationService{

	@PersistenceContext
	private EntityManager em;

	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;

	@Override
	public void persist(ReportDefinition newReport) {

	}

	@Override
	public ReportDefinition findById(long id) {
		return null;
	}

	@Override
	public void update(ReportDefinition reportDef) {
		session().saveOrUpdate(reportDef);
	}

	@Override
	@PreAuthorize("hasPermission(#definition.id, 'org.squashtest.tm.domain.report.ReportDefinition' ,'WRITE') "
		+ OR_HAS_ROLE_ADMIN)
	public void updateDefinition(ReportDefinition definition, ReportDefinition oldDef) {
		definition.setProject(oldDef.getProject());
		((AuditableMixin) definition).setCreatedBy(((AuditableMixin) oldDef).getCreatedBy());
		((AuditableMixin) definition).setCreatedOn(((AuditableMixin) oldDef).getCreatedOn());
		//rename if needed without forgot to rename the node.
		if (!definition.getName().equals(oldDef.getName())) {
			CustomReportLibraryNode node = customReportLibraryNodeService.findNodeFromEntity(oldDef);
			node.renameNode(definition.getName());
		}
		session().flush();
		session().clear();
		update(definition);
	}

	@Override
	public void save(ReportDefinition report) {
		session().save(report);
	}

	private Session session(){
		return em.unwrap(Session.class);
	}
}
