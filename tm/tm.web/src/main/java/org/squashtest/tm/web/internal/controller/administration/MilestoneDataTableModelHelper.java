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
package org.squashtest.tm.web.internal.controller.administration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

public class MilestoneDataTableModelHelper  extends DataTableModelBuilder<Milestone> {

	private MilestoneManagerService milestoneManagerService;

	private InternationalizationHelper messageSource;
	private Locale locale;
	private GenericProject project;
	public Locale getLocale() {
		return locale;
	}

	public void setMilestoneManagerService(MilestoneManagerService milestoneManagerService) {
		this.milestoneManagerService = milestoneManagerService;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public InternationalizationHelper getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;
	}

	public MilestoneDataTableModelHelper(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;
	}

	public MilestoneDataTableModelHelper(InternationalizationHelper messageSource, Locale locale) {

		this.locale = locale;
		this.messageSource = messageSource;
	}



	@Override
	protected Object buildItemData(Milestone item) {
		Map<String, Object> row = new HashMap<>(12);
		final AuditableMixin auditable = (AuditableMixin) item;
		row.put("entity-id", item.getId());
		row.put("index", getCurrentIndex() + 1);
		row.put("label", item.getLabel());
		row.put("nbOfProjects", item.getNbOfBindedProject());
		row.put("description", item.getDescription());
		row.put("range", i18nRange(item.getRange()));
		row.put("owner", ownerToPrint(item));
		row.put("status", i18nStatus(item.getStatus()));
		// Issue 5065 There we check if milestone is binded to the current project, don't care about the others
		Boolean isBoundToThisProject = false;
		if (project != null && milestoneManagerService.isMilestoneBoundToOneObjectOfProject(item, project)) {
			isBoundToThisProject = true;
		}
		row.put("binded-to-objects", messageSource.internationalizeYesNo(isBoundToThisProject, locale));
		row.put("endDate", messageSource.localizeDate(item.getEndDate(), locale).substring(0, 10));
		// Could be done with a SimpleDateFormat but substring works very well.
		row.put("created-on", messageSource.localizeDate(auditable.getCreatedOn(), locale).substring(0, 10));
		row.put("created-by", auditable.getCreatedBy());
		row.put("last-mod-on", messageSource.localizeDate(auditable.getLastModifiedOn(), locale));
		row.put("last-mod-by", auditable.getLastModifiedBy());
		row.put("bindableToObject", item.getStatus().isBindableToObject());
		row.put("delete", "");
		row.put("checkbox", "");

		return row;
	}

	private Object ownerToPrint(Milestone item) {
		String owner = null;
		if (item.getRange() == MilestoneRange.GLOBAL){
			owner = messageSource.internationalize("label.milestone.global.owner", locale);
		} else {
			owner = item.getOwner().getName();
		}
		return owner;
	}

	private String i18nRange(final MilestoneRange milestoneRange){
		final String i18nKey = milestoneRange.getI18nKey();
		return  messageSource.internationalize(i18nKey, locale);
	}

private String i18nStatus(final MilestoneStatus milestoneStatus){
	final String i18nKey = milestoneStatus.getI18nKey();
	return  messageSource.internationalize(i18nKey, locale);
	}



public void setProject(GenericProject project) {
	this.project = project;

}

}
