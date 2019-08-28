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
package org.squashtest.tm.service.internal.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.audit.AuditModificationService;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service("squashtest.tm.service.AuditModificationService")
@Transactional
public class AuditModificationServiceImpl implements AuditModificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditModificationServiceImpl.class);

	private static final List<BindableEntity> AUDITABLE_BINDABLE_ENTITY = Arrays.asList(
		BindableEntity.CAMPAIGN, BindableEntity.CAMPAIGN_FOLDER, BindableEntity.TEST_CASE, BindableEntity.TESTCASE_FOLDER,
		BindableEntity.REQUIREMENT_VERSION, BindableEntity.REQUIREMENT_FOLDER,
		BindableEntity.ITERATION, BindableEntity.TEST_SUITE, BindableEntity.EXECUTION, BindableEntity.EXECUTION_STEP);

	@Inject
	private AttachmentListDao attachmentListDao;

	@Override
	public void updateRelatedToAttachmentAuditableEntity(long attachmentListId){
		LOGGER.debug("Looking for auditable related to attachment list with ID {}", attachmentListId);
		AuditableMixin auditable = attachmentListDao.findAuditableAssociatedEntityIfExists(attachmentListId);
		if(auditable != null){
			updateAuditable(auditable);
		}
	}

	@Override
	public void updateRelatedToRequirementLinkAuditableEntity(List<RequirementVersion> versions){
		LOGGER.debug("Updating requirement version auditable related to a RequirementLink");

		versions.stream().map(version -> (AuditableMixin)version).forEach(this::updateAuditable);
	}

	@Override
	public void updateRelatedToCustomFieldAuditableEntity(BoundEntity boundEntity){
		LOGGER.debug("Updating auditable related to CUF BoundEntity {}", boundEntity);
		if(AUDITABLE_BINDABLE_ENTITY.contains(boundEntity.getBoundEntityType())){
			updateAuditable((AuditableMixin) boundEntity);
		} else if(boundEntity.getBoundEntityType().equals(BindableEntity.TEST_STEP)){
			//[TM-418] Safe cast: CallTestStep doesn't implement BoundEntity
			ActionTestStep testStep = (ActionTestStep) boundEntity;
			updateAuditable((AuditableMixin) testStep.getTestCase());
		}
	}

	@Override
	public void updateAuditable(AuditableMixin auditable){
		LOGGER.debug("Updating auditable {}", auditable);
		auditable.setLastModifiedOn(new Date());
		auditable.setLastModifiedBy(UserContextHolder.getUsername());
	}
}
