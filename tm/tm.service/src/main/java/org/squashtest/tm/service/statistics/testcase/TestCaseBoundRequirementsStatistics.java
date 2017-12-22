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
package org.squashtest.tm.service.statistics.testcase;

public final class TestCaseBoundRequirementsStatistics{
	
	private int zeroRequirements = 0;
	private int oneRequirement = 0;
	private int manyRequirements = 0;
	
	public int getZeroRequirements() {
		return zeroRequirements;
	}
	
	
	public void setZeroRequirements(int zeroRequirements) {
		this.zeroRequirements = zeroRequirements;
	}

	public int getOneRequirement() {
		return oneRequirement;
	}

	public void setOneRequirement(int oneRequirement) {
		this.oneRequirement = oneRequirement;
	}

	public int getManyRequirements() {
		return manyRequirements;
	}

	public void setManyRequirements(int manyRequirements) {
		this.manyRequirements = manyRequirements;
	}

	public TestCaseBoundRequirementsStatistics(){
		super();
	}


	public TestCaseBoundRequirementsStatistics(int zeroRequirements,
			int oneRequirement, int manyRequirements) {
		super();
		this.zeroRequirements = zeroRequirements;
		this.oneRequirement = oneRequirement;
		this.manyRequirements = manyRequirements;
	}
	
	
	
}