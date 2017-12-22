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
package oslcdomain;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.bugtracker.advanceddomain.AdvancedProject;
import org.squashtest.tm.bugtracker.advanceddomain.FieldValue;
import org.squashtest.tm.bugtracker.definition.RemoteCategory;
import org.squashtest.tm.bugtracker.definition.RemoteFieldStub;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemotePriority;
import org.squashtest.tm.bugtracker.definition.RemoteStatus;
import org.squashtest.tm.bugtracker.definition.RemoteUser;
import org.squashtest.tm.bugtracker.definition.RemoteVersion;

public class OslcIssue implements RemoteIssue {

	//maps a fieldId to a FieldValue
	private Map<String, FieldValue> fieldValues = new HashMap<>();

	private AdvancedProject project;

	private String id;

	private String btName;

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	private URL url;
	//the name of the fields scheme currently used, see AdvancedProject#schemes
	private String currentScheme;

	private String selectDialog;

	private String createDialog;

	public String getSelectDialog() {
		return selectDialog;
	}

	public void setSelectDialog(String selectDialog) {
		this.selectDialog = selectDialog;
	}

	public String getCreateDialog() {
		return createDialog;
	}

	public void setCreateDialog(String createDialog) {
		this.createDialog = createDialog;
	}


	public void setId(String key) {
		this.id = key;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean hasBlankId() {
		return id == null ||
			id.isEmpty() ||
			id.matches("^\\s*$");
	}


	@Override
	public String getSummary() {
		return findFieldValueName("summary");
	}

	public void setSummary(String summary) {
		if (isFieldNotSet("summary")) {
			addGenericFieldValue("summary", summary);
		}
	}

	public void setAssignee(String assignee) {
		addGenericFieldValue("assignee", assignee);
	}

	public void setStatus(String status) {
		addGenericFieldValue("status", status);
	}

	public void setPriority(String priority) {
		addGenericFieldValue("priority", priority);
	}

	@Override
	public String getDescription() {
		return findFieldValueName("description");
	}

	@Override
	public void setDescription(String description) {
		if (isFieldNotSet("description")) {
			addGenericFieldValue("description", description);
		}
	}

	@Override
	public String getComment() {
		return findFieldValueName("comment");
	}

	@Override
	public void setComment(String comment) {
		if (isFieldNotSet("comment")) {
			addGenericFieldValue("comment", comment);
		}
	}

	@Override
	public AdvancedProject getProject() {
		return project;
	}


	public void setProject(AdvancedProject project) {
		this.project = project;
	}

	@Override
	public RemoteStatus getStatus() {
		return fieldValues.get("status");
	}


	@Override
	public RemoteUser getAssignee() {
		RemoteUser user = fieldValues.get("assignee");
		if (user == null) {
			user = new RemoteFieldStub();
		}
		return user;
	}

	@Override
	public RemotePriority getPriority() {
		RemotePriority priority = fieldValues.get("priority");
		if (priority == null) {
			priority = new RemoteFieldStub();
		}
		return priority;
	}

	@Override
	public RemoteCategory getCategory() {
		RemoteCategory category = fieldValues.get("category");
		if (category == null) {
			category = new RemoteFieldStub();
		}
		return category;
	}

	@Override
	public RemoteVersion getVersion() {
		RemoteVersion version = fieldValues.get("version");
		if (version == null) {
			version = new RemoteFieldStub();
		}
		return version;
	}

	@Override
	public void setBugtracker(String btName) {
		this.btName = btName;
	}

	@Override
	public String getBugtracker() {
		return btName;
	}

	@Override
	public String getNewKey() {
		return null;
	}

	public void setFieldValue(String fieldName, FieldValue fieldValue) {
		fieldValues.put(fieldName, fieldValue);
	}

	public FieldValue getFieldValue(String fieldName) {
		return fieldValues.get(fieldName);
	}

	public void setFieldValues(Map<String, FieldValue> fieldValues) {
		this.fieldValues = fieldValues;
	}

	public Map<String, FieldValue> getFieldValues() {
		return fieldValues;
	}

	public String getCurrentScheme() {
		return currentScheme;
	}

	public void setCurrentScheme(String currentScheme) {
		this.currentScheme = currentScheme;
	}

	// ********************* private stuffs ***************************

	private boolean isFieldNotSet(String name) {
		return fieldValues.get(name) == null;
	}

	private String findFieldValueName(String fieldId) {
		FieldValue value = fieldValues.get(fieldId);
		return value != null ? value.getName() : "";
	}

	private void addGenericFieldValue(String fieldName, String value) {
		FieldValue newValue = new FieldValue(fieldName, fieldName, value);
		fieldValues.put(fieldName, newValue);
	}
}
