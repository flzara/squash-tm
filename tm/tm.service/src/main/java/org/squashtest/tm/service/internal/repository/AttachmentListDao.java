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
package org.squashtest.tm.service.internal.repository;

import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

public interface AttachmentListDao {
	AttachmentList getOne(Long id);

	TestCase findAssociatedTestCaseIfExists(Long attachmentListId);

	RequirementVersion findAssociatedRequirementVersionIfExists(Long attachmentListId);

	/**
	 * Find the auditable entity, among eligible entities (see {@link org.squashtest.tm.service.internal.repository.hibernate.HibernateAttachmentListDao#entityClassMap}, associated to the given attachment list.
	 * Record object return by Jooq is enough to further exploitation.
	 * @param attachmentListId the given attachment list id
	 * @return an {@link AuditableMixin} object or null if the associated entity is not from the eligible type (test case, campaign, requirement version).
	 */
	AuditableMixin findAuditableAssociatedEntityIfExists(Long attachmentListId);
}
