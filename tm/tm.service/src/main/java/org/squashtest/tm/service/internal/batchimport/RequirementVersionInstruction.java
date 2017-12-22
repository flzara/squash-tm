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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.requirement.RequirementVersion;

public class RequirementVersionInstruction extends Instruction<RequirementVersionTarget> implements CustomFieldHolder, Milestoned {

	private RequirementVersion requirementVersion;
	private final Map<String, String> customFields = new HashMap<>();
	private final String[] milestones = {};

	/**
	 * Used to avoid exception during postprocess. If something went wrong in import process,
	 * postprocess on this instruction should not be performed
	 */
	private boolean fatalError = false;



	public RequirementVersionInstruction(RequirementVersionTarget target, RequirementVersion requirementVersion) {
		super(target);
		this.requirementVersion = requirementVersion;
	}

	@Override
	protected LogTrain executeUpdate(Facility facility) {
		return facility.updateRequirementVersion(this);
	}

	@Override
	protected LogTrain executeDelete(Facility facility) {
		return facility.deleteRequirementVersion(this);
	}

	@Override
	protected LogTrain executeCreate(Facility facility) {
		return facility.createRequirementVersion(this);
	}

	@Override
	public void addCustomField(String code, String value) {
		customFields.put(code, value);
	}

	public RequirementVersion getRequirementVersion() {
		return requirementVersion;
	}

	public void setRequirementVersion(RequirementVersion requirementVersion) {
		this.requirementVersion = requirementVersion;
	}

	public Map<String, String> getCustomFields() {
		return customFields;
	}

	@Override
	public List<String> getMilestones() {
		return Arrays.asList(milestones);
	}

	public boolean isFatalError() {
		return fatalError;
	}

	public void fatalError() {
		this.fatalError = true;
	}



}
