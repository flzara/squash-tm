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

public final class TestCaseImportanceStatistics {
	private int veryHigh = 0;
	private int high = 0;
	private int medium = 0;
	private int low = 0;
	
	public int getVeryHigh() {
		return veryHigh;
	}
	
	public void setVeryHigh(int veryHigh) {
		this.veryHigh = veryHigh;
	}
	
	public int getHigh() {
		return high;
	}
	
	public void setHigh(int high) {
		this.high = high;
	}
	
	public int getMedium() {
		return medium;
	}
	
	public void setMedium(int medium) {
		this.medium = medium;
	}
	
	public int getLow() {
		return low;
	}
	
	public void setLow(int low) {
		this.low = low;
	}

	public TestCaseImportanceStatistics(int veryHigh, int high, int medium,
			int low) {
		super();
		this.veryHigh = veryHigh;
		this.high = high;
		this.medium = medium;
		this.low = low;
	}
	
	public TestCaseImportanceStatistics() {
		super();
	}
	
}