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
package org.squashtest.tm.web.internal.controller.report;

import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.report.ReportDefinition;

import java.util.*;

@SuppressWarnings("Duplicates")
public class JsonReportInstance {

	private String name;

	private String description;

	private String summary;

	private String pluginNamespace;

	private String parameters;

	private String label;

	private Boolean isDocx;

	private int pdfViews;

	private Map<String, List<String>> reportAttributes;

	private String createdBy;

	private String lastModifiedBy;

	private Date createdOn;

	private Date lastModifiedOn;

	private Long projectId;

	private Long ownerId;

	public JsonReportInstance() {
		super();
	}

	public JsonReportInstance(ReportDefinition def) {
		this.name = def.getName();
		this.description = def.getDescription();
		this.summary = def.getSummary();
		this.pluginNamespace = def.getPluginNamespace();
		this.parameters = def.getParameters();
		this.projectId = def.getProject().getId();
		this.ownerId = def.getOwner().getId();
		doAuditableAttributes(def);

	}

	private void doAuditableAttributes(ReportDefinition def) {
		AuditableMixin audit = (AuditableMixin) def;
		this.createdBy = audit.getCreatedBy();
		this.lastModifiedBy = audit.getLastModifiedBy();
		this.createdOn = audit.getCreatedOn();
		this.lastModifiedOn = audit.getLastModifiedOn();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPluginNamespace() {
		return pluginNamespace;
	}

	public void setPluginNamespace(String pluginNamespace) {
		this.pluginNamespace = pluginNamespace;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public Map<String, List<String>> getReportAttributes() {
		return reportAttributes;
	}

	public void setReportAttributes(Map<String, List<String>> reportAttributes) {
		this.reportAttributes = reportAttributes;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public Boolean getDocx() {
		return isDocx;
	}

	public void setDocx(Boolean docx) {
		isDocx = docx;
	}

	public int getPdfViews() {
		return pdfViews;
	}

	public void setPdfViews(int pdfViews) {
		this.pdfViews = pdfViews;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}
}
