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

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sun.org.apache.xml.internal.serializer.utils.AttList;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class HibernateAttachmentListDao implements AttachmentListDao {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AttachmentList findOne(Long id) {
		return entityManager.getReference(AttachmentList.class, id);
	}

	@Override
	public TestCase findAssociatedTestCaseIfExists(Long attachmentListId) {
		final QTestCase testCase = QTestCase.testCase;

		return new JPAQueryFactory(entityManager)
			.selectFrom(testCase)
			.where(testCase.attachmentList.id.eq(attachmentListId))
			.fetchOne();
	}

	@Override
	public RequirementVersion findAssociatedRequirementVersionIfExists(Long attachmentListId) {
		final QRequirementVersion req = QRequirementVersion.requirementVersion;

		return new JPAQueryFactory(entityManager)
			.selectFrom(req)
			.where(req.attachmentList.id.eq(attachmentListId))
			.fetchOne();
	}
}
