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

import org.jooq.Record2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.audit.AuditModificationService;
import org.squashtest.tm.service.internal.repository.AttachmentListDao;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service("squashtest.tm.service.AuditModificationService")
@Transactional
public class AuditModificationServiceImpl implements AuditModificationService {

	private static final List<BindableEntity> auditableBindableEntity = Arrays.asList(BindableEntity.CAMPAIGN, BindableEntity.TEST_CASE, BindableEntity.REQUIREMENT_VERSION);

	@Inject
	private AttachmentListDao attachmentListDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private CampaignDao campaignDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	public void updateRelatedToAttachmentAuditableEntity(long attachmentListId){
		Record2<String, Long> result = attachmentListDao.findAuditableAssociatedEntityIfExists(attachmentListId);

		if(result != null){
			String entityName = result.get("entity_name", String.class);
			long entityId = result.get("entity_id", Long.class);

			AuditableMixin auditable = null;
			switch (entityName){
				case "test_case":
					auditable = (AuditableMixin) testCaseDao.findById(entityId);
					break;
				case "campaign":
					auditable = (AuditableMixin) campaignDao.findById(entityId);
					break;
				case "requirement_version":
					Optional<RequirementVersion> option = requirementVersionDao.findById(entityId);
					if(option.isPresent()){
						auditable = (AuditableMixin) option.get();
					}
					break;
			}
			if(auditable != null){
				updateAuditable(auditable);
			}
		}
	}

	public void updateRelatedToRequirementLinkAuditableEntity(List<RequirementVersion> versions){
		versions.stream().map(version -> (AuditableMixin)version).forEach(this::updateAuditable);
	}

	public void updateRelatedToCustomFieldAuditableEntity(BoundEntity boundEntity){
		if(auditableBindableEntity.contains(boundEntity.getBoundEntityType())){
			updateAuditable((AuditableMixin) boundEntity);
		}
	}

	private void updateAuditable(AuditableMixin auditable){
		auditable.setLastModifiedOn(new Date());
		auditable.setLastModifiedBy(UserContextHolder.getUsername());
	}
}
