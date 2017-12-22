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

public class TestCaseSizeStatistics {

	private int zeroSteps = 0;
	private int between0And10Steps = 0;
	private int between11And20Steps = 0;
	private int above20Steps = 0;
	
	public int getZeroSteps() {
		return zeroSteps;
	}
	
	public void setZeroSteps(int zeroSteps) {
		this.zeroSteps = zeroSteps;
	}
	
	public int getBetween0And10Steps() {
		return between0And10Steps;
	}
	
	public void setBetween0And10Steps(int between0And10Steps) {
		this.between0And10Steps = between0And10Steps;
	}
	
	public int getBetween11And20Steps() {
		return between11And20Steps;
	}
	
	public void setBetween11And20Steps(int between11And20Steps) {
		this.between11And20Steps = between11And20Steps;
	}
	
	public int getAbove20Steps() {
		return above20Steps;
	}
	
	public void setAbove20Steps(int above20Steps) {
		this.above20Steps = above20Steps;
	}
	
}