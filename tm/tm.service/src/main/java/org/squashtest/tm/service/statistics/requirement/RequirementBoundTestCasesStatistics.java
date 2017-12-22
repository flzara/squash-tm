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
package org.squashtest.tm.service.statistics.requirement;

public final class RequirementBoundTestCasesStatistics {
	
	private int zeroTestCases = 0;
	private int oneTestCase = 0;
	private int manyTestCases = 0;
	
	public RequirementBoundTestCasesStatistics() {
		super();
	}
	public RequirementBoundTestCasesStatistics(int zeroTestCases,
			int oneTestCase, int manyTestCases) {
		super();
		this.zeroTestCases = zeroTestCases;
		this.oneTestCase = oneTestCase;
		this.manyTestCases = manyTestCases;
	}
	
	public int getZeroTestCases() {
		return zeroTestCases;
	}
	public void setZeroTestCases(int zeroTestCases) {
		this.zeroTestCases = zeroTestCases;
	}

	public int getOneTestCase() {
		return oneTestCase;
	}
	public void setOneTestCase(int oneTestCase) {
		this.oneTestCase = oneTestCase;
	}

	public int getManyTestCases() {
		return manyTestCases;
	}
	public void setManyTestCases(int manyTestCases) {
		this.manyTestCases = manyTestCases;
	}

}