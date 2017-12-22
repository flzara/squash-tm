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

public final class RequirementValidationStatistics {
	
	private int conclusiveUndefined;
	private int conclusiveMinor;
	private int conclusiveMajor;
	private int conclusiveCritical;

	private int inconclusiveUndefined;
	private int inconclusiveMajor;
	private int inconclusiveMinor;
	private int inconclusiveCritical;

	private int undefinedUndefined;
	private int undefinedMajor;
	private int undefinedMinor;
	private int undefinedCritical;
	
	public RequirementValidationStatistics(int conclusiveUndefined, int conclusiveMinor, int conclusiveMajor,
			int conclusiveCritical, int inconclusiveUndefined, int inconclusiveMajor, int inconclusiveMinor,
			int inconclusiveCritical, int undefinedUndefined, int undefinedMajor, int undefinedMinor,
			int undefinedCritical) {
		super();
		this.conclusiveUndefined = conclusiveUndefined;
		this.conclusiveMinor = conclusiveMinor;
		this.conclusiveMajor = conclusiveMajor;
		this.conclusiveCritical = conclusiveCritical;
		this.inconclusiveUndefined = inconclusiveUndefined;
		this.inconclusiveMajor = inconclusiveMajor;
		this.inconclusiveMinor = inconclusiveMinor;
		this.inconclusiveCritical = inconclusiveCritical;
		this.undefinedUndefined = undefinedUndefined;
		this.undefinedMajor = undefinedMajor;
		this.undefinedMinor = undefinedMinor;
		this.undefinedCritical = undefinedCritical;
	}
	public RequirementValidationStatistics() {
		super();
	}

	public int getConclusiveUndefined() {
		return conclusiveUndefined;
	}
	public void setConclusiveUndefined(int conclusiveUndefined) {
		this.conclusiveUndefined = conclusiveUndefined;
	}

	public int getConclusiveMinor() {
		return conclusiveMinor;
	}
	public void setConclusiveMinor(int conclusiveMinor) {
		this.conclusiveMinor = conclusiveMinor;
	}

	public int getConclusiveMajor() {
		return conclusiveMajor;
	}
	public void setConclusiveMajor(int conclusiveMajor) {
		this.conclusiveMajor = conclusiveMajor;
	}

	public int getConclusiveCritical() {
		return conclusiveCritical;
	}
	public void setConclusiveCritical(int conclusiveCritical) {
		this.conclusiveCritical = conclusiveCritical;
	}

	public int getInconclusiveUndefined() {
		return inconclusiveUndefined;
	}
	public void setInconclusiveUndefined(int inconclusiveUndefined) {
		this.inconclusiveUndefined = inconclusiveUndefined;
	}

	public int getInconclusiveMajor() {
		return inconclusiveMajor;
	}
	public void setInconclusiveMajor(int inconclusiveMajor) {
		this.inconclusiveMajor = inconclusiveMajor;
	}

	public int getInconclusiveMinor() {
		return inconclusiveMinor;
	}
	public void setInconclusiveMinor(int inconclusiveMinor) {
		this.inconclusiveMinor = inconclusiveMinor;
	}

	public int getInconclusiveCritical() {
		return inconclusiveCritical;
	}
	public void setInconclusiveCritical(int inconclusiveCritical) {
		this.inconclusiveCritical = inconclusiveCritical;
	}

	public int getUndefinedUndefined() {
		return undefinedUndefined;
	}
	public void setUndefinedUndefined(int undefinedUndefined) {
		this.undefinedUndefined = undefinedUndefined;
	}

	public int getUndefinedMajor() {
		return undefinedMajor;
	}
	public void setUndefinedMajor(int undefinedMajor) {
		this.undefinedMajor = undefinedMajor;
	}

	public int getUndefinedMinor() {
		return undefinedMinor;
	}
	public void setUndefinedMinor(int undefinedMinor) {
		this.undefinedMinor = undefinedMinor;
	}

	public int getUndefinedCritical() {
		return undefinedCritical;
	}
	public void setUndefinedCritical(int undefinedCritical) {
		this.undefinedCritical = undefinedCritical;
	}
	
}