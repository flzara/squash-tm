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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;


import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.service.internal.batchimport.Facility;
import org.squashtest.tm.service.internal.batchimport.Instruction;
import org.squashtest.tm.service.internal.batchimport.LogTrain;

public class CoverageInstruction extends Instruction<CoverageTarget> {

	private RequirementVersionCoverage coverage;

	public RequirementVersionCoverage getCoverage() {
		return coverage;
	}

	public void setCoverage(RequirementVersionCoverage coverage) {
		this.coverage = coverage;
	}

	protected CoverageInstruction(CoverageTarget target, RequirementVersionCoverage coverage) {
		super(target);
		this.coverage = coverage;
	}

	@Override
	protected LogTrain executeUpdate(Facility facility) {
		// for now we can just create coverage.
		return executeCreate(facility);
	}

	@Override
	protected LogTrain executeDelete(Facility facility) {
		// for now we can just create coverage.
		return executeCreate(facility);
	}

	@Override
	protected LogTrain executeCreate(Facility facility) {
		return facility.createCoverage(this);
	}

}
