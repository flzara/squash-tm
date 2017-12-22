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

public final class RequirementCriticalityStatistics {
	
	private int undefined;
	private int minor;
	private int major;
	private int critical;
	
	public RequirementCriticalityStatistics(int undefined, int minor, int major, int critical) {
		super();
		this.undefined = undefined;
		this.minor = minor;
		this.major = major;
		this.critical = critical;
	}
	public RequirementCriticalityStatistics() {
		super();
	}
	
	public int getUndefined() {
		return undefined;
	}
	public void setUndefined(int undefined) {
		this.undefined = undefined;
	}
	
	public int getMinor() {
		return minor;
	}
	public void setMinor(int minor) {
		this.minor = minor;
	}
	
	public int getMajor() {
		return major;
	}
	public void setMajor(int major) {
		this.major = major;
	}
	
	public int getCritical() {
		return critical;
	}
	public void setCritical(int critical) {
		this.critical = critical;
	}

}