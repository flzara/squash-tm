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

import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.testcase.TestCaseFolder;

import java.util.HashMap;
import java.util.Map;


public class NewFolderDto {

	/*@NotBlank*/
	private String name;

	private String description;

	/*@NotNull
	@NotEmpty*/
	//maps a CustomField id to the value of a corresponding CustomFieldValue
	private Map<Long, RawValue> customFields = new HashMap<>();

	public NewFolderDto() {
		super();
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
		this.description = description;
	}

	public Map<Long, RawValue> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(Map<Long, RawValue> customFields) {
		this.customFields = customFields;
	}

	public Folder<?> toFolder(EntityType type) {
		Folder<?> folder;
		switch(type) {
			case CAMPAIGN_FOLDER:
				folder = new CampaignFolder();
				break;
			case REQUIREMENT_FOLDER:
				folder = new RequirementFolder();
				break;
			case TEST_CASE_FOLDER:
				folder = new TestCaseFolder();
				break;
			default: throw new IllegalArgumentException("Entity of type " + type.name() + " is not supported");
		}
		folder.setName(name);
		folder.setDescription(description);

		return folder;
	}

}
