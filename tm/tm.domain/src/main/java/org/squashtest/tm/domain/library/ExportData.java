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
package org.squashtest.tm.domain.library;

import java.util.Date;

import org.squashtest.tm.domain.audit.AuditableMixin;

/**
 * Common DataSource for jasper Node Export
 *
 * @author mpagnon
 *
 */
public abstract class ExportData {

	private Long id;
	private String folderName = "";// TODO Confusing ! this is not the folder name : it is the folder path !
	private String project;
	private String name;
	private String description;
	private Date createdOn;
	private String createdBy;
	private Long folderId;
	public static final Long NO_FOLDER = -1L;

	public ExportData() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		doSetDescription(description);
	}

	private void doSetDescription(String description) {
		if (description != null) {
			this.description = description;
		}
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		doSetFolderName(folderName);
	}

	private void doSetFolderName(String folderName) {
		if (folderName != null) {
			this.folderName = folderName;
		}
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Long getFolderId() {
		return folderId;
	}

	public void setFolderId(Long folderId) {
		this.folderId = folderId;
	}

	public ExportData(LibraryNode node, Folder folder) {
		this.id = node.getId();
		this.name = node.getName();
		doSetDescription(node.getDescription());
		this.project = node.getProject().getName();
		AuditableMixin audit = (AuditableMixin) node;
		this.createdOn = audit.getCreatedOn();
		this.createdBy = audit.getCreatedBy();
		// folder is null if the requirement is located directly under the project root.
		if (folder == null) {
			this.folderId = NO_FOLDER;
		} else {
			this.folderId = folder.getId();
			doSetFolderName(folder.getName());
		}
	}

	public ExportData(LibraryNode node) {
		this.id = node.getId();
		this.name = node.getName();
		doSetDescription(node.getDescription());
		this.project = node.getProject().getName();
		AuditableMixin audit = (AuditableMixin) node;
		this.createdOn = audit.getCreatedOn();
		this.createdBy = audit.getCreatedBy();
		// folder is null if the requirement is located directly under the project root.
		this.folderId = NO_FOLDER;
	}

}
