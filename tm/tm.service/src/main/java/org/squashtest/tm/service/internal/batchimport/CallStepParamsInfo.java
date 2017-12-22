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

import org.squashtest.tm.domain.testcase.ParameterAssignationMode;

public final class CallStepParamsInfo{

	public static final CallStepParamsInfo DEFAULT = new CallStepParamsInfo();

	private String calledDatasetName = null;
	private ParameterAssignationMode paramMode = ParameterAssignationMode.NOTHING;

	public CallStepParamsInfo(String calledDatasetName, ParameterAssignationMode paramMode) {
		super();
		this.calledDatasetName = calledDatasetName;
		this.paramMode = paramMode;
	}

	public CallStepParamsInfo() {
		super();
	}

	public String getCalledDatasetName() {
		return calledDatasetName;
	}

	public ParameterAssignationMode getParamMode() {
		return paramMode;
	}

	public void setCalledDatasetName(String calledDatasetName) {
		this.calledDatasetName = calledDatasetName;
	}

	public void setParamMode(ParameterAssignationMode paramMode) {
		this.paramMode = paramMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (calledDatasetName == null ? 0 : calledDatasetName.hashCode());
		result = prime * result + (paramMode == null ? 0 : paramMode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CallStepParamsInfo other = (CallStepParamsInfo) obj;
		if (calledDatasetName == null) {
			if (other.calledDatasetName != null) {
				return false;
			}
		} else if (!calledDatasetName.equals(other.calledDatasetName)) {
			return false;
		}
		if (paramMode != other.paramMode) {
			return false;
		}
		return true;
	}



}
