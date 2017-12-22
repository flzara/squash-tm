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
package org.squashtest.tm.domain.requirement;

import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.infolist.ListItemReference;

/**
 * Data holder for requirement version creation. We cannot use a requirement version because of its constrained
 * relationship with a requirement.
 * 
 * @author Gregory Fouquet
 * 
 */
public class NewRequirementVersionDto {
	
	/*@NotBlank*/
	private String name;

	private String description;

	/*@NotNull*/
	private RequirementCriticality criticality = RequirementCriticality.UNDEFINED;

	/*@NotNull*/
	private String category;

	/*@Length(max=50)*/
	private String reference;

	/*@NotNull
	@NotEmpty*/
	//maps a CustomField id to the value of a corresponding CustomFieldValue
	private Map<Long, RawValue> customFields = new HashMap<>();
	
	
	
	public NewRequirementVersionDto() {
		super();
	}
	
	/**
	 * Constructor used by import requirement process. As the exel parser return a {@link RequirementVersion}
	 * we need to convert it to DTO before persist the new requirement version
	 * @param requirementVersion
	 */
	public NewRequirementVersionDto(RequirementVersion requirementVersion, Map<Long, RawValue> customFields){
		this.name = requirementVersion.getName();
		this.reference = requirementVersion.getReference();
		this.description = requirementVersion.getDescription();
		this.criticality = requirementVersion.getCriticality();
		this.category = requirementVersion.getCategory().getCode();
		this.customFields = customFields;
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

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Map<Long, RawValue> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(Map<Long, RawValue> customFields) {
		this.customFields = customFields;
	}




	public RequirementVersion toRequirementVersion() {
		RequirementVersion version = new RequirementVersion();
		version.setName(name);
		if (criticality != null) {
			version.setCriticality(criticality);
		}

		version.setReference(reference);
		version.setDescription(description);
		version.setCategory(new ListItemReference(category));

		return version;
	}



}
