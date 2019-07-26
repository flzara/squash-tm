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
package org.squashtest.tm.service.audit;

import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.requirement.RequirementVersion;

import java.util.List;

/**
 * This service give methods to modify audit of auditable entities on modification of entities which are associated to these entities.
 * It is used when {@link org.squashtest.tm.service.internal.hibernate.AuditLogInterceptor} can not do the job to modify audit.
 */
public interface AuditModificationService {
	/**
	 * Update last modified on and las modified by attribute of auditable related to the given attachment list (for now test case, campaign or requirement version)
	 * @param attachmentListId the modified attachment list id
	 */
	void updateRelatedToAttachmentAuditableEntity(long attachmentListId);

	/**
	 * Update last modified on and las modified by of given {@link RequirementVersion}
	 * @param versions the given {@link RequirementVersion} list
	 */
	void updateRelatedToRequirementLinkAuditableEntity(List<RequirementVersion> versions);

	/**
	 * Update last modified on and las modified by of given {@link BoundEntity} if auditable
	 * @param boundEntity the given {@link BoundEntity}
	 */
	void updateRelatedToCustomFieldAuditableEntity(BoundEntity boundEntity);

	/**
	 * Update last modified on and las modified by of given {@link AuditableMixin}
	 * @param auditableMixin the given {@link AuditableMixin}
	 */
	void updateAuditable(AuditableMixin auditableMixin);

}
