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
package org.squashtest.tm.web.internal.controller.search.advanced.tablemodels;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

public class RequirementSearchResultDataTableModelBuilder extends DataTableModelBuilder<RequirementVersion> {

	private boolean isInAssociationContext;
	private Set<Long> associatedRequirementIds;
	private InternationalizationHelper messageSource;
	private PermissionEvaluationService permissionService;
	private Locale locale;

	private boolean isInAssociationContext() {
		return this.isInAssociationContext;
	}

	private String formatStatus(RequirementStatus status, Locale locale) {
		return status.getLevel() + "-" + messageSource.internationalize(status, locale);
	}

	private String formatCriticality(RequirementCriticality criticality, Locale locale) {
		return criticality.getLevel() + "-" + messageSource.internationalize(criticality, locale);
	}

	private String formatInfoItem(InfoListItem item, Locale locale) {
		return messageSource.getMessage(item.getLabel(), null, item.getLabel(), locale);
	}

	public RequirementSearchResultDataTableModelBuilder(Locale locale, InternationalizationHelper messageSource,
			PermissionEvaluationService permissionService, boolean isInAssociationContext,
			Set<Long> associatedTestCaseIds) {

		this.locale = locale;
		this.permissionService = permissionService;
		this.messageSource = messageSource;
		this.isInAssociationContext = isInAssociationContext;
		this.associatedRequirementIds = associatedTestCaseIds;
	}

	@Override
	protected Map<String, Object> buildItemData(RequirementVersion item) {

		final AuditableMixin auditable = (AuditableMixin) item;
		Map<String, Object> res = new HashMap<>();
		res.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getProject().getName());
		res.put("project-id", item.getProject().getId());
		if (isInAssociationContext()) {
			res.put("empty-is-associated-holder", " ");
			res.put("is-associated", associatedRequirementIds.contains(item.getId()));
		}
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put("requirement-id", item.getRequirement().getId());
		res.put("requirement-version-id", item.getId());
		res.put("requirement-reference",  HtmlUtils.htmlEscape(item.getReference()));
		res.put("requirement-label",  HtmlUtils.htmlEscape(item.getName()));
		res.put("editable", isRequirementVersionEditable(item));
		res.put("requirement-criticality", formatCriticality(item.getCriticality(), locale));
		res.put("requirement-category", formatInfoItem(item.getCategory(), locale));
		res.put("requirement-status", formatStatus(item.getStatus(), locale));
		res.put("requirement-milestone-nb", item.getMilestones().size());
		res.put("requirement-version", item.getVersionNumber());
		res.put("requirement-version-nb", item.getRequirement().getRequirementVersions().size());
		res.put("requirement-testcase-nb", item.getVerifyingTestCases().size());
		res.put("requirement-attachment-nb", item.getAttachmentList().size());
		res.put("requirement-created-by", formatUsername(auditable.getCreatedBy()));
		res.put("requirement-modified-by", formatUsername(auditable.getLastModifiedBy()));
		res.put("empty-openinterface2-holder", " ");
		res.put("empty-opentree-holder", " ");
		return res;
	}

	private boolean isRequirementVersionEditable(RequirementVersion item) {
		return item.isModifiable() && permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", item);
	}

}

