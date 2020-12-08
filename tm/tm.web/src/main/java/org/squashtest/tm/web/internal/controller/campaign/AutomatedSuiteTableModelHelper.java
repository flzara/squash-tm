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
package org.squashtest.tm.web.internal.controller.campaign;


import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedSuite;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AutomatedSuiteTableModelHelper extends DataTableModelBuilder<AutomatedSuite> {

	/**
	 * The source for localized label messages.
	 */
	private final InternationalizationHelper i18nHelper;

	/**
	 * The locale to use to format the labels.
	 */
	private final Locale locale;

	public AutomatedSuiteTableModelHelper(@NotNull Locale locale, @NotNull InternationalizationHelper i18nHelper) {
		super();
		this.locale = locale;
		this.i18nHelper = i18nHelper;
	}

	@Override
	protected Map<String, Object> buildItemData(AutomatedSuite suite) {

		Map<String, Object> res = new HashMap<>();
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		res.put("uuid", suite.getId());
		AuditableMixin audit = (AuditableMixin) suite;
		res.put("created-on", i18nHelper.localizeDate(audit.getCreatedOn(), locale));
		res.put("created-by", HTMLCleanupUtils.escapeOrDefault(audit.getCreatedBy(), null));
		res.put("last-modified-on", i18nHelper.localizeDate(audit.getLastModifiedOn(), locale));
		res.put("last-modified-by", HTMLCleanupUtils.escapeOrDefault(audit.getLastModifiedBy(), null));
		res.put("status", suite.getExecutionStatus().getCanonicalStatus());
		res.put("has-executions", !suite.getExecutionExtenders().isEmpty());
		res.put("result-urls", getResultURLList(suite));
		res.put("attachment-list-id", suite.getAttachmentList().getId());
		res.put("attachment-list", getAttachmentDtos(suite));
		return res;
	}

	private List<URL> getResultURLList(AutomatedSuite automatedSuite){
		return automatedSuite.getExecutionExtenders().stream()
			.map(AutomatedExecutionExtender::getResultURL).filter(Objects::nonNull)
			.distinct().collect(Collectors.toList());
	}

	private Set<AttachmentDTO> getAttachmentDtos(AutomatedSuite suite){
		Set<Attachment> suiteAttachments = suite.getAttachmentList().getAllAttachments();
		Set<Attachment> execAttachments = new HashSet<>();
		suite.getExecutionExtenders().forEach(execExtender -> {
			Set<Attachment> attachments = execExtender.getExecution().getAttachmentList().getAllAttachments();
			execAttachments.addAll(attachments);
		});
		Set<Attachment> allAttachments = new HashSet<>(suiteAttachments);
		allAttachments.addAll(execAttachments);
		return allAttachments.stream().map(attachment -> new AttachmentDTO(attachment.getId(), HtmlUtils.htmlEscape(attachment.getName()))).collect(Collectors.toSet());
	}

	private class AttachmentDTO{
		private final Long id;

		private final String name;

		public AttachmentDTO(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}
}
