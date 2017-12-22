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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.service.internal.dto.RawValueModel;
import org.squashtest.tm.service.internal.dto.RawValueModel.RawValueModelMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TestStepUpdateFormModel {

	private String action;
	private String expectedResult;

	private RawValueModelMap cufValues = new RawValueModelMap();

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}



	public RawValueModelMap getCufValues() {
		return cufValues;
	}

	public void setCufValues(RawValueModelMap cufValues) {
		this.cufValues = cufValues;
	}

	@JsonIgnore
	public Map<Long, RawValue> getCufs(){
		Map<Long, RawValue> cufs = new HashMap<>(cufValues.size());
		for (Entry<Long, RawValueModel> entry : cufValues.entrySet()){
			cufs.put(entry.getKey(), entry.getValue().toRawValue());
		}
		return cufs;
	}


}
