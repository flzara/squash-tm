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
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

public class TestCaseSearchResultDataTableModelBuilder extends DataTableModelBuilder<TestCase> {
	private InternationalizationHelper messageSource;
	private Locale locale;
	private PermissionEvaluationService permissionService;
	private IterationModificationService iterationService;
	private boolean isInAssociationContext;
	private Set<Long> associatedTestCaseIds;

	public TestCaseSearchResultDataTableModelBuilder(Locale locale, InternationalizationHelper messageSource,
			PermissionEvaluationService permissionService, IterationModificationService iterationService,
			boolean isInAssociationContext, Set<Long> associatedTestCaseIds) {
		this.locale = locale;
		this.messageSource = messageSource;
		this.permissionService = permissionService;
		this.iterationService = iterationService;
		this.isInAssociationContext = isInAssociationContext;
		this.associatedTestCaseIds = associatedTestCaseIds;
	}

	private String formatImportance(TestCaseImportance importance, Locale locale) {

		return importance.getLevel() + "-" + messageSource.internationalize(importance, locale);
	}

	private String formatStatus(TestCaseStatus status, Locale locale) {
		return status.getLevel() + "-" + messageSource.internationalize(status, locale);
	}

	private boolean isTestCaseEditable(TestCase item) {
		return item.isModifiable() && permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", item);
	}

	private boolean isInAssociationContext() {
		return this.isInAssociationContext;
	}

	@Override
	public Map<String, Object> buildItemData(TestCase item) {
		final AuditableMixin auditable = (AuditableMixin) item;
		Map<String, Object> res = new HashMap<>();
		res.put(DataTableModelConstants.PROJECT_NAME_KEY, item.getProject().getName());
		res.put("project-id", item.getProject().getId());
		if (isInAssociationContext()) {
			res.put("empty-is-associated-holder", " ");
			res.put("is-associated", associatedTestCaseIds.contains(item.getId()));
		}
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put("test-case-id", item.getId());
		res.put("test-case-ref", HtmlUtils.htmlEscape(item.getReference()));
		res.put("test-case-label", HtmlUtils.htmlEscape(item.getName()));
		res.put("editable", isTestCaseEditable(item));
		res.put("test-case-weight", formatImportance(item.getImportance(), locale));
		res.put("test-case-weight-auto", item.getImportanceAuto());
		res.put("test-case-nature", formatInfoItem(item.getNature(), locale));
		res.put("test-case-type", formatInfoItem(item.getType(), locale));
		res.put("test-case-status", formatStatus(item.getStatus(), locale));
		res.put("test-case-milestone-nb", item.getMilestones().size());
		res.put("test-case-requirement-nb", item.getVerifiedRequirementVersions().size());
		res.put("test-case-teststep-nb", item.getSteps().size());
		res.put("test-case-iteration-nb", iterationService.findIterationContainingTestCase(item.getId()).size());
		res.put("test-case-attachment-nb", item.getAllAttachments().size());
		res.put("test-case-created-by", formatUsername(auditable.getCreatedBy()));
		res.put("test-case-modified-by", formatUsername(auditable.getLastModifiedBy()));
		res.put("empty-openinterface2-holder", " ");
		res.put("empty-opentree-holder", " ");
		return res;
	}

	private String formatInfoItem(InfoListItem item, Locale locale) {
		return messageSource.getMessage(item.getLabel(), null, item.getLabel(), locale);
	}
}

